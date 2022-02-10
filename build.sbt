import play.core.PlayVersion.akkaVersion
import play.core.PlayVersion.akkaHttpVersion
import play.grpc.gen.scaladsl.{PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator}
import com.typesafe.sbt.packager.docker.{Cmd, CmdLike, DockerAlias, ExecCmd}
import play.scala.grpc.sample.BuildInfo

name := """arbitrage"""
organization := "com.botocrypt"

version := "1.0-SNAPSHOT"

// Arbitrage API spec constants
lazy val arbitrageApiSpecRepoUrl = "https://raw.githubusercontent.com/marko-domic/botocrypt.proto"
lazy val arbitrageApiSpecRelativePath = "proto/arbitrage_service"
lazy val arbitrageApiSpecFileName = "arbitrage.proto"
lazy val arbitrageApiSpecVersion = "0.2"

// Proto files URLs
lazy val arbitrageServiceApiSpecUrl = s"$arbitrageApiSpecRepoUrl/$arbitrageApiSpecVersion/" +
  s"$arbitrageApiSpecRelativePath/$arbitrageApiSpecFileName"

lazy val protoFiles: Map[String, String] =
  Map(arbitrageApiSpecFileName -> arbitrageServiceApiSpecUrl)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(AkkaGrpcPlugin) // enables source generation for gRPC
  .enablePlugins(PlayAkkaHttp2Support) // enables serving HTTP/2 and gRPC
  // #grpc_play_plugins
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    // #grpc_client_generators
    // build.sbt
    akkaGrpcExtraGenerators += PlayScalaClientCodeGenerator,
    // #grpc_client_generators
    // #grpc_server_generators
    // build.sbt
    akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator,
    // #grpc_server_generators
    PlayKeys.devSettings ++= Seq(
      "play.server.http.port" -> "disabled",
      "play.server.https.port" -> "9443",
      // Configures the keystore to use in Dev mode. This setting is equivalent to `play.server.https.keyStore.path`
      // in `application.conf`.
      "play.server.https.keyStore.path" -> "conf/selfsigned.keystore"
    )
  )
  .settings(
    // workaround to https://github.com/akka/akka-grpc/pull/470#issuecomment-442133680
    dockerBaseImage := "openjdk:8-alpine",
    dockerCommands  :=
      Seq.empty[CmdLike] ++
        Seq(
          Cmd("FROM", "openjdk:8-alpine"),
          ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
        ) ++
        dockerCommands.value.tail ,
    Docker / dockerAliases += DockerAlias(None, None, "play-scala-grpc-example", None),
    Docker / packageName := "play-scala-grpc-example"
  )
  .settings(
    libraryDependencies ++= CompileDeps ++ TestDeps
  )

val CompileDeps = Seq(
  guice,
  "com.lightbend.play"      %% "play-grpc-runtime"    % BuildInfo.playGrpcVersion,
  "com.typesafe.akka"       %% "akka-discovery"       % akkaVersion,
  "com.typesafe.akka"       %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"       %% "akka-http-spray-json" % akkaHttpVersion,
  // Test Database
  "com.h2database" % "h2" % "1.4.199"
)

val playVersion = play.core.PlayVersion.current
val TestDeps = Seq(
  "com.lightbend.play"      %% "play-grpc-scalatest" % BuildInfo.playGrpcVersion % Test,
  "com.lightbend.play"      %% "play-grpc-specs2"    % BuildInfo.playGrpcVersion % Test,
  "com.typesafe.play"       %% "play-test"           % playVersion     % Test,
  "com.typesafe.play"       %% "play-specs2"         % playVersion     % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play"  % "5.0.0" % Test
)

// Task for removing all proto files from src/main/protobuf
lazy val cleanupProtobufDirectory = taskKey[Unit]("Cleanup protobuf directory")
cleanupProtobufDirectory := {
  import scala.reflect.io.Directory

  streams.value.log.info("Protobuf directory cleanup started.")

  // Create protobuf directory path (src/main/protobuf)
  val srcDirectory = (Compile / sourceDirectory).value
  val protobufDirectoryPath = srcDirectory / "protobuf"

  // Fetch protobuf directory and remove all files from it
  val protobufDirectory = new Directory(protobufDirectoryPath)
  if (protobufDirectory.exists && protobufDirectory.isDirectory) {
    protobufDirectory.files.foreach(file => file.deleteRecursively)
  }

  streams.value.log.info("Finished with protobuf directory cleanup.")
}

// Task for downloading all necessary proto files in src/main/protobuf
lazy val downloadProtoFiles =
  taskKey[Unit]("Download proto files from remote repository into local proto directory")
downloadProtoFiles := {
  import scala.sys.process._

  streams.value.log.info("Downloading proto files from remote repository started.")

  // Create destination protobuf directory path (src/main/protobuf)
  val srcDirectory = (Compile / sourceDirectory).value
  val destinationProtobufDirectory = srcDirectory / "protobuf"

  if (!destinationProtobufDirectory.exists || !destinationProtobufDirectory.isDirectory) {
    destinationProtobufDirectory.mkdir()
  }

  // Download all proto files in destination directory
  for ((protoFileName, protoFileUrl) <- protoFiles) {
    url(s"$protoFileUrl") #> (destinationProtobufDirectory / s"$protoFileName") !
  }

  streams.value.log.info(s"Finished with downloading proto files in $destinationProtobufDirectory.")
}

scalaVersion := "2.13.8"
scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked")

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Set task dependency chain
Compile / compile := (Compile / compile).dependsOn(
  downloadProtoFiles.dependsOn(cleanupProtobufDirectory)).value

// Make verbose tests
Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.botocrypt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.botocrypt.binders._"
