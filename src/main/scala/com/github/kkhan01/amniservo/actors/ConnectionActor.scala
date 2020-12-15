package com.github.kkhan01.amniservo.actors

import java.net.Socket
import java.io.PrintStream

import akka.actor.{Actor, PoisonPill}

class ConnectionActor(fn: (Map[String, String]) => String, params: Map[String, String]) extends Actor {
  var out: java.io.PrintStream = _

  override def preStart(): Unit = {
    println("Started processing connection.")
  }

  override def postStop(): Unit = {
    println("Ended processing connection.")
  }

  // TODO: implement real http access codes
  def respond(s: String): String = "HTTP/1.0 200 OK\n\r\n" + s

  def send(s: String): Unit = out.println(respond(s))

  def receive = {
    case socket: Socket => {
      out = new PrintStream(socket.getOutputStream())

      try {
        val fnRes = fn(params)
        send(fnRes)
      } catch {
        case err: Throwable => {
          println("Debug: error in connection actor:")
          println(err)
          send("Something went wrong while processing.")
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
