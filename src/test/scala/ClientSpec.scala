import Client.Command
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect._
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, TestInbox}
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import org.slf4j.event.Level
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Random

object Client {
  val random = new Random()

  sealed trait Command
  case class JobSubmit(who: ActorRef[String]) extends Command
  case class CheckStatus(who: ActorRef[String]) extends Command
  case class StopClusterWork(who: ActorRef[String]) extends Command

  def apply(): Behaviors.Receive[Command] = Behaviors.receiveMessage {
    case JobSubmit(who) => who ! "Job submitted"
      Behaviors.same
    case CheckStatus(who) =>
      if (random.nextBoolean()) who ! "Job in progress" else who ! "Job failed"
      Behaviors.same
    case StopClusterWork(who) =>
      who ! "All Jobs correctly finished"
      who ! "Cluster was shutdown"
      Behaviors.same
  }
  //#under-test

}




class ClientSpec extends AnyWordSpec with Matchers {

  "Typed probe actor" must {

    "send back the message - Job submitted with Synchronous testing" in {
      val testKit: BehaviorTestKit[Command] = BehaviorTestKit(Client())
      val inbox: TestInbox[String] = TestInbox[String]()

      testKit.run(Client.JobSubmit(inbox.ref))

      val expectedResult = "Job submitted"

      inbox.expectMessage(expectedResult)
    }

    "send back the message - Job submitted with Asynchronous testing" in {
      val kit: ActorTestKit = ActorTestKit()

      val client: ActorRef[Command] = kit.spawn(Client())
      val probe = kit.createTestProbe[String]()

      client ! Client.JobSubmit(probe.ref)
      client ! Client.JobSubmit(probe.ref)

      probe.expectMessage("Job submitted")
      probe.expectMessage("Job submitted")
    }


    "send back the message with test probe - any status message" in {
      val kit = ActorTestKit()

      val client = kit.spawn(Client())
      val probe = kit.createTestProbe[String]()

      client ! Client.JobSubmit(probe.ref)
      client ! Client.JobSubmit(probe.ref)

      //      probe.expectMessage("Job submitted")

      probe.expectMessageType[String]

      probe.receiveMessages(1)
    }


    "checking state" in {

//      val mockedBehavior = Behaviors.receiveMessage[Message] { msg =>
//        msg.replyTo ! Success(msg.i)
//        Behaviors.same
//      }
//      val probe = testKit.createTestProbe[Message]()
//      val mockedPublisher = testKit.spawn(Behaviors.monitor(probe.ref, mockedBehavior))

    }
  }
}