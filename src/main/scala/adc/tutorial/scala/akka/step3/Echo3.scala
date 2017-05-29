package adc.tutorial.scala.akka.step3

import akka.actor.{Actor, ActorLogging}


class Echo3 extends Actor with ActorLogging {
  import Echo3._

  override def unhandled(message: Any): Unit = {
    throw new Exception(s" received unknown message:  ${message.getClass.getName}")
  }

  override def receive: Receive = {
    case Message(content) => sender() ! content
  }
}

object Echo3 {
  case class Message(content: String)
}
