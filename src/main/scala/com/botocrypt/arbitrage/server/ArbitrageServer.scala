package com.botocrypt.arbitrage.server

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.pki.pem.{DERPrivateKeyLoader, PEMDecoder}
import com.botocrypt.arbitrage.actor.init.SystemInitializer
import com.botocrypt.arbitrage.api.ArbitrageServiceHandler

import java.security.cert.{Certificate, CertificateFactory}
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

case class ArbitrageServer(context: ActorContext[SystemInitializer.Initializing]) {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys = context.system
    implicit val ec: ExecutionContext = context.system.executionContext

    context.log.info("Initializing arbitrage gRPC server")

    val service: HttpRequest => Future[HttpResponse] =
      ArbitrageServiceHandler(new ArbitrageServiceImpl())(context.system)

    val bound: Future[Http.ServerBinding] = Http(context.system)
      .newServerAt(interface = "127.0.0.1", port = 8080)
      .enableHttps(serverHttpContext)
      .bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        println(s"gRPC server bound to ${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        println("Failed to bind gRPC endpoint, terminating system", ex)
        context.system.terminate()
    }

    context.log.info("Initializing arbitrage gRPC server")

    bound
  }

  private def serverHttpContext: HttpsConnectionContext = {
    val privateKey =
      DERPrivateKeyLoader.load(PEMDecoder.decode(readPrivateKeyPem()))
    val fact = CertificateFactory.getInstance("X.509")
    val cer = fact.generateCertificate(
      classOf[ArbitrageServer].getResourceAsStream("/certs/server1.pem")
    )
    val ks = KeyStore.getInstance("PKCS12")
    ks.load(null)
    ks.setKeyEntry(
      "private",
      privateKey,
      new Array[Char](0),
      Array[Certificate](cer)
    )
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, null)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
    ConnectionContext.httpsServer(context)
  }

  private def readPrivateKeyPem(): String =
    Source.fromResource("certs/server1.key").mkString
}
