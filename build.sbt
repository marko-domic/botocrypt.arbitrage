name := "arbitrage"

version := "0.1"

scalaVersion := "2.13.7"

val akkaVersion = "2.6.18"
val logbackVersion = "1.2.10"

// Arbitrage API spec constants
val arbitrageApiSpecRepoUrl = "https://raw.githubusercontent.com/marko-domic/botocrypt.proto"
val arbitrageApiSpecRelativePath = "proto/arbitrage_service"
val arbitrageApiSpecFileName = "arbitrage.proto"
val arbitrageApiSpecVersion = "0.2"

// Proto files URLs
val arbitrageServiceApiSpecUrl = s"$arbitrageApiSpecRepoUrl/$arbitrageApiSpecVersion/" +
  s"$arbitrageApiSpecRelativePath/$arbitrageApiSpecFileName"

val protoFiles: Map[String, String] = Map(arbitrageApiSpecFileName -> arbitrageServiceApiSpecUrl)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

// Task for removing all proto files from src/main/proto
lazy val cleanupProtoDirectory = taskKey[Unit]("Cleanup proto directory")
cleanupProtoDirectory := {
  import scala.reflect.io.Directory

  streams.value.log.info("Proto directory cleanup started.")

  // Create proto directory path (src/main/proto)
  val srcDirectory = (Compile / sourceDirectory).value
  val protoDirectoryPath = srcDirectory / "proto"

  // Fetch proto directory and remove all files from it
  val protoDirectory = new Directory(protoDirectoryPath)
  if (protoDirectory.exists && protoDirectory.isDirectory) {
    protoDirectory.files.foreach(file => file.deleteRecursively)
  }

  streams.value.log.info("Finished with proto directory cleanup.")
}

// Task for downloading all necessary proto files in src/main/proto
lazy val downloadProtoFiles =
  taskKey[Unit]("Download proto files from remote repository into local proto directory")
downloadProtoFiles := {
  import scala.sys.process._

  streams.value.log.info("Downloading proto files from remote repository started.")

  // Create destination proto directory path (src/main/proto)
  val srcDirectory = (Compile / sourceDirectory).value
  val destinationProtoDirectory = srcDirectory / "proto"

  if (!destinationProtoDirectory.exists || !destinationProtoDirectory.isDirectory) {
    destinationProtoDirectory.mkdir()
  }

  // Download all proto files in destination directory
  for ((protoFileName, protoFileUrl) <- protoFiles) {
    url(s"$protoFileUrl") #> (destinationProtoDirectory / s"$protoFileName") !
  }

  streams.value.log.info(s"Finished with downloading proto files in $destinationProtoDirectory.")
}

// Set task dependency chain
Compile / compile := (Compile / compile).dependsOn(
  downloadProtoFiles.dependsOn(cleanupProtoDirectory)).value
