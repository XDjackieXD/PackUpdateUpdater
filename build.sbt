name := "UpdaterUpdater"

version := "3.0"

scalaVersion := "2.12.8"

libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.json" % "json" % "20180813"

Compile / mainClass := Some("at.chaosfield.updaterupdater.UpdaterUpdater")