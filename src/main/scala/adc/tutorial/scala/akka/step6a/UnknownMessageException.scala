package adc.tutorial.scala.akka.step6a

import akka.actor.ActorRef

case class PersistenceException(destination: String, reason: Throwable) extends Exception(s"could not persist to $destination", reason)

case class FailedMessageException(askedBy: ActorRef, reason: Throwable) extends Exception(s"could not process message", reason)

case class UnknownMessageException(askedBy: ActorRef, messageType: String) extends Exception(s"received unknown message of type $messageType") { }
