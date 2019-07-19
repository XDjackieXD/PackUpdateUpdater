package at.chaosfield.packupdate.common

import java.net.URI

class Component(val name: String, val version: String, val downloadUrl: Option[URI], val componentType: ComponentType, val hash: Option[FileHash], val flags: Array[ComponentFlag]) {
  def toCSV = {
    Array(
      name,
      version,
      downloadUrl.map(_.toString).getOrElse(""),
      componentType.stringValue,
      hash.getOrElse(""),
      flags.map(_.internalName).mkString(";")
    ).mkString(",")
  }

  def neededOnSide(packSide: PackSide): Boolean = {
    packSide match {
      case PackSide.Client => !flags.contains(ComponentFlag.ServerOnly)
      case PackSide.Server => !flags.contains(ComponentFlag.ClientOnly)
    }
  }

  def display = s"$name $version"
}

object Component {
  def fromCSV(data: Array[String]) = {
    new Component(
      data(0), // name
      data(1), // version
      if (data(2).isEmpty) None else Some(new URI(data(2))), // downloadUrl
      ComponentType.fromString(data(3)).getOrElse(ComponentType.Unknown), // componentType
      data.lift(4).filter(_ != "").map(new FileHash(_)), // hash
      data.lift(5).map(_.split(';')).getOrElse(Array.empty[String]).flatMap(ComponentFlag.fromString) // flags
    )
  }
}
