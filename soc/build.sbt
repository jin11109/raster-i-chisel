scalaVersion := "2.13.10"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:reflectiveCalls"
)
val chiselVersion = "5.0.0"
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
libraryDependencies ++= Seq(
  "org.chipsalliance" %% "chisel" % chiselVersion,
  "edu.berkeley.cs" %% "chiseltest" % "5.0.2" % "test"
)
