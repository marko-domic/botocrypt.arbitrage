val playGrpcVersion = "0.9.1"
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.13")
addSbtPlugin("com.lightbend.akka.grpc" %% "sbt-akka-grpc" % "2.1.3")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcVersion
