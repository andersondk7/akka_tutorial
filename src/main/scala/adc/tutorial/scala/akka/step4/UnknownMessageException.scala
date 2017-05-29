package adc.tutorial.scala.akka.step4

import akka.actor.ActorRef

case class UnknownMessageException(sender: ActorRef, messageType: String) extends Exception(s"recevied unknown message of type $messageType") { }
