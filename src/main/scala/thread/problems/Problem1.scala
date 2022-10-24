package thread.problems

object Problem1 extends App {

  case class Account(@volatile private var amount: Int) {
    def withdraw(money: Int) = {this.amount -= money}

    def deposit(money: Int) = {this.amount += money}
  }

  val account = Account(2000)

  for(_ <- 1 to 100000) {
    new Thread(() => account.withdraw(1)).start()
  }

  for(_ <- 1 to 100000) {
    new Thread(() => account.deposit(1)).start()
  }

  println(s"The amount is ${account}")
}

