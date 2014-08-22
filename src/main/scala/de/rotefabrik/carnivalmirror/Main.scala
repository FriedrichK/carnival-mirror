package de.rotefabrik.carnivalmirror

import akka.actor.{ ActorSystem, Inbox, Props, ActorRef }
import scala.concurrent.duration._

import de.rotefabrik.carnivalmirror.actors.Greeter
import de.rotefabrik.carnivalmirror.messages.{ Greet, Greeting, WhoToGreet }

object Main {

	def main(args: Array[String]) = {
		val system = ActorSystem("helloakka")
		val greeter = system.actorOf(Props[Greeter], "greeter")
		val inbox = Inbox.create(system)

		greeter.tell(WhoToGreet("akka"), ActorRef.noSender)

		inbox.send(greeter, Greet)

		val Greeting(message1) = inbox.receive(5.seconds)
		println(s"Greeting: $message1")
	}
		
}