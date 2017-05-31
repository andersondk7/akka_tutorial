package adc.tutorial.scala.akka.step5a

import adc.tutorial.scala.akka.step5a.Echo5a.{CountReport, Counts, Message}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class Echo5aSpec extends FunSpec with Matchers {
  implicit val timeout = Timeout(.5.seconds)
  private val system = ActorSystem("step5")
  // note: internally a supervisor is used but externally it is simply an actor ref
  private val echo = system.actorOf(Props[Echo5aSupervisor], "supervisor")

  describe("echo5a") {
    it("should return recognized message") {
      val sentContent = "its in a bottle"
      val call = echo ? Message(sentContent)
      val received = Await.result(call.mapTo[Message], timeout.duration)
      received.content shouldBe sentContent
    }
    it("should receive different message an unknown message") {
      val sentContent = "its in a bottle"
      val call = echo ? sentContent
      val received = Await.result(call.mapTo[Message], timeout.duration)
      received.content should not be sentContent
    }
    it("should count good and bad messages") {
      val sentContent = "its in a bottle"
      val messageCall = echo ? Message(sentContent)
      val received = Await.result(messageCall.mapTo[Message], timeout.duration)
      received.content shouldBe sentContent
      val countCall = echo ? CountReport
      val countReceived = Await.result(countCall.mapTo[Counts], timeout.duration)
      countReceived.good shouldBe 2
      countReceived.bad shouldBe 1
    }
  }

}
