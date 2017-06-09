package adc.tutorial.scala.akka.step7

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
  * This actor delegates blocking calls to a service that returns Futures
  * @param askedBy who is making the request
  * @param storageService blocking service that implements request
  */
class Echo7(askedBy: ActorRef, storageService: StorageService) extends Actor with ActorLogging {
  import Echo7._
  import Echo7Supervisor._

  override def unhandled(message: Any): Unit = {
    throw UnknownMessageException(sender(), message.getClass.getName)
  }

  override def receive: Receive = {

    // -------------------
    // messages from client
    // -------------------
    case Message(content) =>
      pipe(storageService.postContent(content)) to self

    case ForceFailure =>
      pipe(storageService.forceFailure()) to self

    case StorageSizeRequest =>
      pipe(storageService.storageSize) to self

    // -------------------
    // messages back from service
    // -------------------
    case rt: Try[_] => rt.fold(
      // failure
        {
          case pe: PersistenceException =>
            log.info(s"${self.path} received PersistenceException, throwing FailedMessage with askedBy: ${askedBy.path}")
            throw FailedMessageException(askedBy, pe)
          case x => throw x
        }
      ,
      // success
        response => {
          askedBy ! response
          context.parent ! GoodMessage
          context.stop(self) // we are done here

        }
      )
  }
}

object Echo7 {
  def props(askedBy: ActorRef, storageService: StorageService): Props = Props(classOf[Echo7], askedBy, storageService)

  // ---------------------------------
  // -- requests --
  // ---------------------------------
  sealed trait Request {}
  case object StorageSizeRequest extends Request
  case class Message(content: String) extends Request
  case object ForceFailure extends Request

  // ---------------------------------
  // -- responses --
  // ---------------------------------
  sealed trait Response {}
  case class StorageLength(size: Long) extends Response
  case class ContentWritten(size: Long) extends Response
}
