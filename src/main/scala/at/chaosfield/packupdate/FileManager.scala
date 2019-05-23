package at.chaosfield.packupdate

import java.io.{BufferedReader, File, FileInputStream, FileNotFoundException, FileOutputStream, FileReader, IOException, InputStream, InputStreamReader, OutputStream, PrintStream}
import java.net.{HttpURLConnection, URL}

import org.apache.commons.io.FileUtils

import scala.util.control.Breaks._
import scala.io.Source

object FileManager {
  final val UserAgent = "PackUpdate Automated Mod Updater"

  //open an online file for reading.
  def getOnlineFile(fileUrl: String) = new BufferedReader(new InputStreamReader(new URL(fileUrl).openStream))

  def deleteLocalFile(fileName: String): Boolean = new File(fileName).delete()

  def deleteLocalFolderContents(path: String): Boolean = try {
    val file = new File(path)
    if (file.exists) {
      FileUtils.cleanDirectory(file)
      true
    } else {
      file.mkdir()
    }
  } catch {
    case _: IOException => false
  }

  def unzipLocalFile(zipFile: String, outputPath: String): Boolean = ???

  /**
    * Calculates the diff between two sets of installed components
    * @param local The currently installed set of local components
    * @param remote The currently installed set of remote components
    * @return A set of updates, this can then be applied as needed
    */
  def getUpdates(local: List[Component], remote: List[Component]): List[Update] = {
    (
      local.flatMap(component => {
        remote.find(c => c.name == component.name) match {
          case Some(remote_version) => if (remote_version.version != component.version) {
            Some(Update.UpdatedComponent(component, remote_version))
          } else {
            None
          }
          case None => Some(Update.RemovedComponent(component))
        }
      })
      ++
      remote.filter(component => !local.exists(c => c.name == component.name)).map(Update.NewComponent)
    )
  }

  def parsePackList(packList: Source): List[Component] =
    packList.getLines().filter(l => l.length() > 0 && !l.startsWith("#")).map(s => Component.fromCSV(s.split(","))).toList

  def retrieveUrl(url: URL): InputStream = {
    println(s"Downloading $url")
    def request(url: URL) = {
      val con = url.openConnection
      con.setRequestProperty("user-Agent", UserAgent)
      con.setConnectTimeout(5000)
      con.setReadTimeout(5000)
      con match {
        case http: HttpURLConnection => {
          http.setInstanceFollowRedirects(false)

        }
        case _ => Unit
      }
      con
    }
    request(url).getInputStream
  }

  def writeStreamToFile(source: InputStream, file: File): Unit = {
    val buf = new Array[Byte](1024)
    val dest = new FileOutputStream(file)
    while (true) {
      val bytesRead = source.read(buf)
      if (bytesRead == -1) {
        break
      }
      println(s"Foo: $bytesRead")
      dest.write(buf, 0, bytesRead)
    }
  }

  def writeMetadata(data: List[Component], localFile: File) = {
    val s = new PrintStream(new FileOutputStream(localFile))
    s.println(data.map(_.toCSV).mkString("\n"))
  }
}