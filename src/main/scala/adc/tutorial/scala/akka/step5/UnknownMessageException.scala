package adc.tutorial.scala.akka.step5

import akka.actor.ActorRef

case class UnknownMessageException(sender: ActorRef, messageType: String) extends Exception(s"received unknown message of type $messageType") { }
