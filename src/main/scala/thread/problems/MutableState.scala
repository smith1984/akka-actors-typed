package thread.problems

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop}
import thread.problems.Solution1.{FunctionalActor, ObjectActor}

object MutableState extends App {

  sealed trait Command
  case class Deposit(v: Int) extends Command
  case class Withdraw(v: Int) extends Command
  case class Get() extends Command


  object Account {
    def apply(am: Int): Behavior[Command] = Behaviors.setup { ctx =>
//      ctx.setLoggerName(s"${ctx.self.path.name}") # Option 1
      val staticMdc = Map("name" -> ctx.self.path.name)

      var amount: Int = 0

      Behaviors.withMdc[Command](staticMdc) {
              Behaviors.receiveMessage[Command] {
                case Deposit(v) =>
                  throw new RuntimeException("Kakogo figa")
                  amount = amount + v
                  ctx.log.info(s"Deposit money $v to $amount. Total state is $amount")
//                  ctx.log.info(s"name = ${ctx.self.path.name}")
                  Behaviors.same

                case Withdraw(v) =>
                  amount = amount - v
                  ctx.log.info(s"Withdraw money $v to $amount. Total state is $amount")
                  Behaviors.same

                case Get() =>
                  ctx.log.info(s"Total get state is $amount");
                  Behaviors.same
              }.receiveSignal {
                case (context, PostStop) =>
                  context.log.error(s"Master Control Program stopped ${context.self.path.name}")
                  Behaviors.same
              }
      }





    }
  }


  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val account1: ActorRef[Command] = ctx.spawn(Account(2000), "account_state_actor")
      val account2: ActorRef[Command] = ctx.spawn(Account(777), "account_state_actor_2")

      account2 ! Get()

      for (_ <- 1 to 1000) {
        account1 ! Deposit(1)
      }
      for (_ <- 1 to 1000) {
        account2 ! Deposit(1)
      }

      account2 ! Get()

      for (_ <- 1 to 1000) {
        account1 ! Withdraw(1)
      }
      for (_ <- 1 to 1000) {
        account2 ! Withdraw(1)
      }

      account1 ! Get()
      account2 ! Get()

      Behaviors.same
    }


  val value = MutableState.apply()
  implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

  Thread.sleep(10000)


  system.terminate()
}
