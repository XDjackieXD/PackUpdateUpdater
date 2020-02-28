name := "PackUpdateUpdater"

version := "3.0-rc1"

scalaVersion := "2.12.8"

libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.json" % "json" % "20180813"

Compile / mainClass := Some("at.chaosfield.updaterupdater.UpdaterUpdater")

assemblyMergeStrategy in assembly := {
  case PathList("scala", xs @ _*) => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}