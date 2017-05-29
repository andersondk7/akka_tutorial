package adc.tutorial.scala.akka.step4


import adc.tutorial.scala.akka.step4.Echo4.Message
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class Echo4Spec extends FunSpec with Matchers {
  implicit val timeout = Timeout(.5.seconds)
  private val system = ActorSystem("step4")
  // note: internally a supervisor is used but externally it is simply an actor ref
  private val echo = system.actorOf(Props[Echo4Supervisor], "supervisor")

  describe("echo4") {
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
  }

}
