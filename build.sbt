import com.typesafe.sbt.packager.docker.{Cmd, CmdLike, DockerAlias, ExecCmd}
import play.core.PlayVersion.akkaVersion
import play.grpc.gen.scaladsl.{PlayScalaClientCodeGenerator, PlayScalaServerCodeGenerator}
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

name := """arbitrage"""
organization := "com.botocrypt"

version := "1.0-SNAPSHOT"

dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

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
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcExtraGenerators += PlayScalaClientCodeGenerator,
    akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator,
    PlayKeys.devSettings ++= Seq(
      "play.server.http.port" -> "9000",
      "play.server.https.port" -> "disabled"
    )
  )
  .settings(
    // workaround to https://github.com/akka/akka-grpc/pull/470#issuecomment-442133680
    dockerBaseImage := "openjdk:8-alpine",
    dockerCommands :=
      Seq.empty[CmdLike] ++
        Seq(
          Cmd("FROM", "openjdk:8-alpine"),
          ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
        ) ++
        dockerCommands.value.tail,
    Docker / dockerAliases += DockerAlias(None, None, "play-scala-grpc-example", None),
    Docker / packageName := "play-scala-grpc-example",
  )
  .settings(
    libraryDependencies ++= CompileDeps ++ TestDeps
  )

scalaVersion := "2.13.8"
scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked")

val playVersion = play.core.PlayVersion.current
val playGrpcVersion = "0.9.1"

val CompileDeps = Seq(
  guice,
  "com.lightbend.play" %% "play-grpc-runtime" % playGrpcVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9",
  "com.typesafe.akka" %% "akka-http2-support" % "10.2.9",
  "com.h2database" % "h2" % "2.1.210",
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"
)

val TestDeps = Seq(
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "org.mockito" %% "mockito-scala" % "1.17.5" % Test,
  "com.lightbend.play" %% "play-grpc-scalatest" % playGrpcVersion % Test,
  "com.lightbend.play" %% "play-grpc-specs2" % playGrpcVersion % Test,
  "com.typesafe.play" %% "play-test" % playVersion % Test,
  "com.typesafe.play" %% "play-specs2" % playVersion % Test,
  "org.assertj" % "assertj-core" % "3.22.0" % Test,
  "org.awaitility" % "awaitility" % "4.2.0" % Test
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

excludeFilter := {
  val logs = (baseDirectory.value / "logs").getCanonicalPath
  new SimpleFileFilter(_.getCanonicalPath startsWith logs)
}

// Set task dependency chain
Compile / compile := (Compile / compile).dependsOn(
  downloadProtoFiles.dependsOn(cleanupProtobufDirectory)).value

// Make verbose tests
Test / testOptions := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

// Docker configuration
Docker / maintainer := "marko.domic@outlook.com"
Docker / packageName := "botocrypt/arbitrage"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "0")
Docker / daemonUserUid  := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
dockerBaseImage := "openjdk:8-jre-alpine"
dockerRepository := sys.env.get("ecr_repo")
dockerUpdateLatest := true
