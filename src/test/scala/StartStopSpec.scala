import akka.actor.Kill
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import org.scalatest.wordspec.AnyWordSpecLike


sealed trait Command
case class StartChild(name: String, replyTo: ActorRef[String]) extends Command
case class SendMessageToChild(name: String, msg: String, num: Int) extends Command
case class StopChild(name: String) extends Command
case object Stop extends Command

object Parent {
  def apply(): Behavior[Command] = withChildren(Map())

  def withChildren(childs: Map[String, ActorRef[Command]]): Behavior[Command] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case StartChild(name, replyTo) =>
          ctx.log.info(s"Start child $name")
          val newChild = ctx.spawn(Child(), name)
          replyTo ! name
          withChildren(childs + (name -> newChild))
        case msg @ SendMessageToChild(name, _, i) =>
          ctx.log.info(s"Sending msg to child $name num=$i")
          val childOption = childs.get(name)
          childOption.foreach(childRef => childRef ! msg)

          Behaviors.same
        case StopChild(name) =>
          ctx.log.info(s"Stopping child with the name $name")
          val childOption = childs.get(name)
          childOption.foreach(childRef => ctx.stop(childRef))
          Behaviors.same
        case Stop =>
          ctx.log.info(s"Stopping parent")
          Behaviors.stopped
      }

    }

}

object Child {
  def apply(): Behavior[Command] = Behaviors.setup { ctx =>

    Behaviors.receiveMessage { msg =>
      ctx.log.info(s"Child actor recieved message $msg ")
      Behaviors.same
    }
  }
}

class StartStopSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Typed probe actor" must {

    "send back the message - Job submitted with Synchronous testing" in {
      val parent = spawn(Parent(), "parent")
      val replyProbe = createTestProbe[String]()

      parent ! StartChild("child1", replyProbe.ref)
      parent ! SendMessageToChild("child1", "message to child1", 0)

      parent ! StopChild("child1")

//      parent ! Kill[Command]

      for(i <- 1 to 15) parent ! SendMessageToChild("child1", "message to child1", i)
    }
  }

}
