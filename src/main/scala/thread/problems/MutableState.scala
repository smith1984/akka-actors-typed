package thread.problems

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import thread.problems.Solution1.{FunctionalActor, ObjectActor}

object MutableState extends App {

  sealed trait Command
  case class Deposit(v: Int) extends Command
  case class Withdraw(v: Int) extends Command
  case class Get() extends Command


  object Account {
    def apply(am: Int): Behavior[Command] = Behaviors.setup { ctx =>
      var amount: Int = am

      Behaviors.receiveMessage {
        case Deposit(v) =>
          amount = amount + v
          ctx.log.info(s"Deposit money $v to $amount. Total state is $amount")
          Behaviors.same

        case Withdraw(v) =>
          amount = amount - v
          ctx.log.info(s"Withdraw money $v to $amount. Total state is $amount")
          Behaviors.same

        case Get() =>
          ctx.log.info(s"Total get state is $amount");
          Behaviors.same
      }
    }
  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val account1: ActorRef[Command] = ctx.spawn(Account(2000), "account_state_actor")
      val account2: ActorRef[Command] = ctx.spawn(Account(777), "account_state_actor_2")

      account2 ! Get()

      for(_ <- 1 to 1000) {
        account1 ! Deposit(1)
      }

      account2 ! Get()

      for(_ <- 1 to 1000) {
        account1 ! Withdraw(1)
      }

      account1 ! Get()
      account2 ! Get()

      Behaviors.same
    }


  val value = State()
  implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

}
