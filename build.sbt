name := "baykus"

version := "1.0"

scalaVersion := "2.13.8"

lazy val akkaVersion     = "2.6.19"
lazy val akkaHttpVersion = "10.2.9"

fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka"    %% "akka-actor-typed"     % akkaVersion,
  "com.typesafe.akka"    %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"    %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"    %% "akka-http-spray-json" % akkaHttpVersion,
  "ch.qos.logback"       % "logback-classic"       % "1.2.3",
  "de.jplag"             % "jplag"                 % "3.0.0",
  "org.jsoup"            % "jsoup"                 % "1.15.1",
  "org.scalatest"        %% "scalatest"            % "3.1.0" % Test,
  "org.scalacheck"       %% "scalacheck"           % "1.16.0" % Test,
  "com.github.pathikrit" %% "better-files-akka"    % "3.9.1"
)

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
