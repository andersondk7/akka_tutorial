package adc.tutorial.scala.akka.step5

import akka.actor.{Actor, ActorLogging}

class Echo5 extends Actor with ActorLogging {
  import Echo5._
  var goodCount = 0
  var badCount = 0

  override def unhandled(message: Any): Unit = {
    badCount = badCount + 1
    throw UnknownMessageException(sender(), message.getClass.getName)
  }

  override def receive: Receive = {
    case Message(content) =>
      goodCount = goodCount + 1
      sender() ! Message(content)
    case CountReport =>
      sender() ! Counts(goodCount, badCount)
  }
}

object Echo5 {
  case class Message(content: String)
  case object CountReport
  case class Counts(good: Int, bad: Int)
}
