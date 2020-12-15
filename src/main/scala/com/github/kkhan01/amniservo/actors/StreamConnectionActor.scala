package com.github.kkhan01.amniservo.actors

import java.net.Socket
import java.io.PrintStream

import scala.io.BufferedSource

import akka.actor.{Actor, PoisonPill}

class StreamConnectionActor(fn: (Map[String, String]) => String, params: Map[String, String]) extends Actor {
  override def preStart(): Unit = {
    println("Started processing connection.")
  }

  override def postStop(): Unit = {
    println("Ended processing connection.")
  }

  def receive = {
    case socket: Socket => {
      val in: Iterator[String] = new BufferedSource(socket.getInputStream()).getLines()
      val out: java.io.PrintStream = new PrintStream(socket.getOutputStream())
      var messages = ""

      try {
        while(true){
          val input = in.next()
          messages += input
          println(messages)
          val args = params + ("input" -> input)
          out.println(fn(args))
          out.flush()
        }
      } catch {
        case err: java.util.NoSuchElementException => {}
        case err: Throwable => {
          println("Debug: error in connection actor:")
          println(err)
        }
      }

      socket.close()
      self ! PoisonPill
    }
    case err: Throwable => {
      println("Debug: something went wrong, socket wasn't passed in:")
      println(err)
      self ! PoisonPill
    }
  }
}
