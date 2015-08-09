name := "play_chunks"

scalaVersion := "2.11.1"

resolvers += "ReactiveCouchbase Releases" at "https://raw.github.com/ReactiveCouchbase/repository/master/releases/"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "typesafe_snapshots" at "http://repo.akka.io/snapshots"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  filters,
  "jp.t2v" %% "play2-auth" % "0.13.0" withSources(),
  "org.antlr" % "ST4" % "4.0.7",
  //test
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.12" % "0.1.1" % "test" withSources(),
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.scalatestplus" %% "play" % "1.1.0" % "test" withSources(),
  "jp.t2v" %% "play2-auth-test" % "0.13.0" % "test" withSources(),
  //ES
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" withSources(),
  "com.typesafe.play" %% "play-json" % "2.3.4" withSources()
)
