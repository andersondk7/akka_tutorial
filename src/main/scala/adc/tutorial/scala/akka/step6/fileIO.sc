import java.io.{BufferedWriter, File, FileWriter, IOException}

import scala.util.{Failure, Success, Try}



case class PersistenceException(destination: String, reason: String)
  extends Exception(s"could not persist to $destination because: $reason")

object StorageService {

  // example of the loaner pattern
  def withBufferedWriter(fileName: String)
                        (action: BufferedWriter => Unit): Try[Unit] = Try {
    Try { new BufferedWriter(new FileWriter(fileName, true)) } match {
      case Success(bw) =>
        val work: Try[Unit] = Try { action(bw) }
        bw.flush()
        bw.close()
        work.recover {
          case e: Exception =>
            println(s"done cleaning up")
            throw PersistenceException(fileName, e.getMessage)
        }
      case Failure(t) =>
        println(s"could not create writer")
        throw PersistenceException(fileName, t.getMessage)
    }
  }

  // example of the loaner pattern
  def withFile[T](fileName: String)
                        (action: File => T): Try[T] = Try {
    Try { new File(fileName) } match {
      case Success(file) =>
        if (file.exists) {
          val work: Try[T] = Try {
            action(file)
          }
          work match {
            case Success(i) => i
            case Failure(t) =>
              throw PersistenceException(fileName, t.getMessage)
          }
        }
        else throw PersistenceException(fileName, "does not exist")

      case Failure(t) =>
        throw PersistenceException(fileName, t.getMessage)
      }
  }
}

//val fileName = "/tmp/test.txt"
val fileName = "/test.txt"
def goodWriter(txt: String): (BufferedWriter => Unit) = bw => bw.write(txt)
def badWriter(txt: String): (BufferedWriter => Unit) = _ => {
  val ex = new IOException(s"expected with $txt")
  println(s"throwing $ex")
  throw  ex
}

StorageService.withFile[Long]("/tmp/test.txt")(_.length)
StorageService.withFile[Long]("/tmp/test2.txt")(_.length)

//StorageService.withBufferedWriter(fileName)(goodWriter("first line of text\n")) match {
//  case Success(_) => println(s"first was good")
//  case Failure(t) => println(s"first failed with $t")
//}

//val second = StorageService.withBufferedWriter(fileName)(badWriter("invalid text\n"))
//
//val third = StorageService.withBufferedWriter(fileName)(goodWriter("last line of text\n"))
