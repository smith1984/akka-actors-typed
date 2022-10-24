name := "akka-actors-typed"

version := "0.1"

scalaVersion := "2.13.3"


lazy val akkaVersion       = "2.6.9"
lazy val scalaTestVersion = "3.2.9"

scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed"   % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query"   % akkaVersion,
  "org.scalatest"     %% "scalatest-wordspec"       % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest"     %% "scalatest"                % scalaTestVersion,
)

libraryDependencies += "com.typesafe"   % "config"          % "1.4.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"