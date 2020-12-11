package com.github.kkhan01.amniserver

import java.net._
import java.io._
import scala.io._

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

class HelloActor extends Actor {
  def receive = {
    case connection: java.net.Socket => {

      val in: Iterator[String] = new BufferedSource(connection.getInputStream()).getLines()
      val out: java.io.PrintStream = new PrintStream(connection.getOutputStream())
      var messages: String = ""
      try {
        while(true){
          val input = in.next()
          messages += input
          println(messages)
          out.println(input)
          out.flush()
        }
      } catch {
        case err: java.util.NoSuchElementException => connection.close()
        case err: Throwable =>{
          connection.close()
          println("Debug: unknown error:")
          println(err)
        }
      }
    }
    case _ => println("huh?")
  }
}

object Amniserver {
  def server(): Unit = {
    val PORT = 9999
    val bind = new ServerSocket(PORT)

    val system = ActorSystem("HelloSystem")
    var hi = 1

    while (true) {
      val connection: java.net.Socket = bind.accept()
      val helloActor = system.actorOf(Props[HelloActor], name = hi.toString)
      hi += 1
      helloActor ! connection
    }
  }
}
