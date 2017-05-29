package adc.tutorial.scala.akka.step6


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This actor delegates blocking calls to a service that returns Futures
  * @param askedBy who is making the request
  * @param storageService blocking service that implements request
  */
class Echo6(askedBy: ActorRef, storageService: StorageService) extends Actor with ActorLogging {
  import Echo6._
  import Echo6Supervisor._

  override def unhandled(message: Any): Unit = {
    throw UnknownMessageException(sender(), message.getClass.getName)
  }

  override def receive: Receive = {

    // -------------------
    // messages from client
    // -------------------
    case Message(content) => pipe(storageService.postContent(content)) to self

    case StorageSizeRequest => pipe(storageService.storageSize) to self

    // -------------------
    // messages back from service
    // -------------------
    case cw: ContentWritten =>
      askedBy ! cw
      context.parent ! GoodMessage
      context.stop(self) // we are done here

    case sl: StorageLength =>
      askedBy ! sl
      context.parent ! GoodMessage
      context.stop(self) // we are done here

    case pe: PersistenceException =>
      throw FailedMessageException(askedBy, pe)
  }
}

object Echo6 {
  def props(askedBy: ActorRef, storageService: StorageService): Props = Props(classOf[Echo6], askedBy, storageService)

  // ---------------------------------
  // -- requests --
  // ---------------------------------
  case object StorageSizeRequest
  case class Message(content: String)

  // ---------------------------------
  // -- responses --
  // ---------------------------------
  case class StorageLength(size: Long)
  case class ContentWritten(size: Long)
}
