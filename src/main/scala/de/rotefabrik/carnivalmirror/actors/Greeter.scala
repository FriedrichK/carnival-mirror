package de.rotefabrik.carnivalmirror.actors

import akka.actor.{ Actor }
import de.rotefabrik.carnivalmirror.messages.{ Greet, Greeting, WhoToGreet }

class Greeter extends Actor {
	var greeting = ""

	def receive = {
		case WhoToGreet(who) => greeting = s"hello, $who"
		case Greet => sender ! Greeting(greeting)
	}
}