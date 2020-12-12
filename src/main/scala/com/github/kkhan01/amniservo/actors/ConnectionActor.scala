package com.github.kkhan01.amniservo.actors

import java.net.Socket
import java.io.PrintStream

import scala.io.BufferedSource

import akka.actor.{Actor, PoisonPill}

class ConnectionActor extends Actor {
  override def preStart(): Unit = {
    println("Started connection.")
  }

  override def postStop(): Unit = {
    println("Killed connection.")
  }

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
        case err: java.util.NoSuchElementException => {
          connection.close()
          self ! PoisonPill
        }
        case err: Throwable =>{
          connection.close()
          println("Debug: unknown error:")
          println(err)
          self ! PoisonPill
        }
      }
    }
    case _ => println("huh?")
  }
}
