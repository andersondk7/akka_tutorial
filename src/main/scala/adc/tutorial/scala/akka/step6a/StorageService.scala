package adc.tutorial.scala.akka.step6a


import java.io.{BufferedWriter, File, FileWriter}

import Echo6a.{ContentWritten, StorageLength}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class StorageService(val fileName: String) {

  import StorageService._

  /**
    * Post content to storage
    *
    * @param content the content to be posted
    * @return nothing
    */
  def postContent(content: String): Future[ContentWritten] = Future.fromTry {
    withBufferedWriter[ContentWritten](fileName) { bw => {
      bw.write(content)
      ContentWritten(content.length)
    }}
  }

  def storageSize: Future[StorageLength] = Future.fromTry {
    withFile[StorageLength](fileName) { f => {
      StorageLength(f.length)
    }}
  }

  def resetStorage(): Future[Boolean] = Future.fromTry {
    withFile[Boolean](fileName) { f => {
      f.delete()
    }}
  }

  def forceFailure(): Future[Unit] = Future {
    throw PersistenceException(destination=fileName, reason=new Exception("expected"))
  }
}

object StorageService {

  // this object uses old style java io because it is blocking and
  // I wanted to show a blocking operation

  // example of the loaner pattern
  /**
    * An example of the loaner pattern,
    * <p>
    * This abstracts all of the management of the resource (in this case a BufferedWriter based on a file name)
    * including creation and cleanup in case of error and lets the caller only focus on what to do with
    * the resource once is has been created
    * </p>
    *
    * @param fileName name of file that the BufferedWrite wraps
    * @param action   what to do with the BufferedWriter
    * @return nothing
    */
  def withBufferedWriter[T](fileName: String)
                           (action: BufferedWriter => T): Try[T] = Try {
    Try {
      new BufferedWriter(new FileWriter(fileName, true))
    } match {
      case Success(bw) =>
        // was able to create BufferedWriter, so do the action
        // and clean up (happens even if the action fails)
        val work: Try[T] = Try {
          action(bw)
        }
        bw.flush()
        bw.close()
        work match {
          case Success(r) => r
          case Failure(t) =>
            // could not perform operation on BufferedWriter
            // but still need clean up (which has already happened)
            throw PersistenceException(fileName, t)
        }
      case Failure(t) =>
        // could not create BufferedWriter, so don't clean up
        throw PersistenceException(fileName, t)
    }
  }

  /**
    * Yet another example of a loaner pattern, this time with a file as the resource that is 'loaned'
    *
    * @param fileName name of the loaned file
    * @param action   what to do with the file
    * @return result of the action
    */
  def withFile[T](fileName: String)
                 (action: File => T): Try[T] = Try {
    Try {
      val file = new File(fileName)
      file.createNewFile()
      file
    } match {
      case Success(file) =>
        val work: Try[T] = Try {
          action(file)
        }
        work match {
          case Success(i) => i
          case Failure(t) =>
            throw PersistenceException(fileName, t) // could not perform operation on file
        }

      case Failure(t) =>
        throw PersistenceException(fileName, t)
    }
  }

}

