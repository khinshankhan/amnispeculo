package com.github.kkhan01.amniservo.actors

import java.net.Socket
import java.io.PrintStream

import scala.io.BufferedSource

import akka.actor.{Actor, PoisonPill}

class ConnectionActor extends Actor {
  var connection: java.net.Socket = _
  var in: Iterator[String] = _
  var out: java.io.PrintStream = _

  var messages: String = ""

  override def preStart(): Unit = {
    println("Started connection.")
  }

  override def postStop(): Unit = {
    println("Killed connection.")
  }

  // TODO: refactor out to rest vs stream
  def process() = {
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

  // TODO: refactor out to an actor
  def validate(): Boolean = {
    val input = in.next()
    printf("Validating: %s\n", input)
    return true
  }

  // TODO: return http error
  def invalid(): Unit = {
    out.println("Invalid resource!")
  }

  def receive = {
    case socket: java.net.Socket => {
      connection = socket
      in = new BufferedSource(connection.getInputStream()).getLines()
      out = new PrintStream(connection.getOutputStream())

      val valid = validate()
      if (valid) {
        process()
      } else {
        val _: Unit = invalid()
        self ! PoisonPill
      }
    }
    case _ => println("Debug: something went wrong, socket wasn't passed in.")
  }
}
