package adc.tutorial.scala.akka.step7


import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

class Echo7Spec extends FunSpec with Matchers with BeforeAndAfterAll {
  import Echo7._
  import Echo7Supervisor._
  implicit val timeout = Timeout(10.seconds)
  private val system = ActorSystem("step7")

  describe("echo7") {
    it("should return recognized message") {
      val fileName = "/tmp/echo7.1.txt"
      val service = new StorageService(fileName)
      reset(service)
      val echo = system.actorOf(Echo7Supervisor.props(service), "supervisor7.1")
      val sentContent = "its in a bottle"
      val call = echo ? Message(sentContent)
      val received = Await.result(call.mapTo[ContentWritten], timeout.duration)
      received.size shouldBe sentContent.length
    }
    it("should count storage size") {
      val fileName = "/tmp/echo7.2.txt"
      val service = new StorageService(fileName)
      reset(service)
      val echo = system.actorOf(Echo7Supervisor.props(service), "supervisor7.2")
      val messages = List(
        "text message"
        , "hash tag message"
        , "im message"
        , "standard email message"
        , "old school snail mail message"
      )
      val messageCalls: Future[List[ContentWritten]] = Future.sequence(messages.map(m => (echo ? Message(m)).mapTo[ContentWritten]))
      val totalWritten: Long = Await.result(messageCalls.mapTo[List[ContentWritten]], timeout.duration * messages.size).map(_.size).sum

      val requestedLength = echo ? StorageSizeRequest
      val storageLength = Await.result(requestedLength.mapTo[StorageLength], timeout.duration)
      storageLength.size shouldBe totalWritten
    }
    it("should receive different message an unknown message") {
      val fileName = "/tmp/echo7.3.txt"
      val service = new StorageService(fileName)
      reset(service)
      val echo = system.actorOf(Echo7Supervisor.props(service), "supervisor7.3")
      val sentContent = "its in a bottle"
      val call = echo ? sentContent
      val received = Await.result(call.mapTo[Message], timeout.duration)
      received.content should not be sentContent

      val requestedLength = echo ? StorageSizeRequest
      val storageLength = Await.result(requestedLength.mapTo[StorageLength], timeout.duration)
      storageLength.size shouldBe 0
    }
    it("should count good and bad messages") {
      val fileName = "/tmp/echo7.4.txt"
      val service = new StorageService(fileName)
      reset(service)
      val echo = system.actorOf(Echo7Supervisor.props(service), "supervisor7.4")
      val messages: List[Any] = List(
        Message("text message")
        , Message("hash tag message")
        , Message("im message")
        , "forgot to wrap content"
        , Message("standard email message")
        , 42 // in this case, not the answer to everything...
        , Message("old school snail mail message")
      )
      val goodCount = messages.count(_.isInstanceOf[Message])
      val badCount = messages.size - goodCount
      val messageCalls: Future[List[Any]] = Future.sequence(messages.map(m => echo ? m))
      Await.result(messageCalls, timeout.duration*messages.size) // don't care what we got back in this test
      val countCall = echo ? CountReport
      val counts = Await.result(countCall.mapTo[Counts], timeout.duration)
      counts.good shouldBe goodCount
      counts.bad shouldBe badCount
    }
    it("should count a failure in the service as a 'bad message'") {
      val fileName = "/tmp/echo7.5.txt"
      val service = new StorageService(fileName)
      reset(service)
      val echo = system.actorOf(Echo7Supervisor.props(service), "supervisor7.5")
      val messages: List[Any] = List(
        Message("text message")
        , Message("hash tag message")
        , ForceFailure
        , Message("im message")
        , "forgot to wrap content"
        , Message("standard email message")
        , 42 // in this case, not the answer to everything...
        , Message("old school snail mail message")
      )
      val goodCount = messages.count(_.isInstanceOf[Message])
      val badCount = messages.size - goodCount
      val messageCalls: Future[List[Any]] = Future.sequence(messages.map(m => echo ? m))
      Await.result(messageCalls, timeout.duration*messages.size)// don't care what we got back in this test
      val countCall = echo ? CountReport
      val counts = Await.result(countCall.mapTo[Counts], timeout.duration)
      counts.good shouldBe goodCount
      counts.bad shouldBe badCount
    }
  }

  def reset(service: StorageService): Try[Boolean] = {
    val resetCall = service.resetStorage()
    Await.result( service.resetStorage().mapTo[Try[Boolean]], timeout.duration)
  }
}
