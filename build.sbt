name := "arbitrage"

version := "0.1"

scalaVersion := "2.13.7"

lazy val akkaVersion = "2.6.18"
lazy val akkaHttpVersion = "10.2.7"
lazy val akkaGrpcVersion = "2.1.2"
lazy val logbackVersion = "1.2.10"

enablePlugins(AkkaGrpcPlugin)

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

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-pki" % akkaVersion,

  // The Akka HTTP overwrites are required because Akka-gRPC depends on 10.1.x
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,

  "ch.qos.logback" % "logback-classic" % logbackVersion
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

// Set task dependency chain
Compile / compile := (Compile / compile).dependsOn(
  downloadProtoFiles.dependsOn(cleanupProtobufDirectory)).value
