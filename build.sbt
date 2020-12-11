name := "amnispeculo"

version := "0.0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= {
  val AkkaVersion = "2.6.10"

  Seq(
    "com.typesafe.akka" %% "akka-actor"            % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream"           % AkkaVersion
  )
}
