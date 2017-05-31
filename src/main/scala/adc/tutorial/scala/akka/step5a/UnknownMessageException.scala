package adc.tutorial.scala.akka.step5a

import akka.actor.ActorRef

case class UnknownMessageException(sender: ActorRef, messageType: String) extends Exception(s"received unknown message of type $messageType") { }
