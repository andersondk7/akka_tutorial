package adc.tutorial.scala.akka.step4

import akka.actor.{Actor, ActorLogging}

class Echo4 extends Actor with ActorLogging {
  import Echo4._

  override def unhandled(message: Any): Unit = {
    throw UnknownMessageException(sender(), message.getClass.getName)
  }

  override def receive: Receive = {
    case Message(content) => sender() ! Message(content)
  }
}

object Echo4 {
  case class Message(content: String)
}
