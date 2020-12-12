package com.github.kkhan01.amniservo

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}

import com.github.kkhan01.amniservo.actors.ConnectionActor

class Amniservo {
  var methods = scala.collection.mutable.Map[String, String]()

  def add(key: String, value: String): Unit = {
    if (methods.contains(key)) {
      printf("Debug: Failed to add key: %s\n", key)
    }
    else {
      methods += (key -> value)
    }
  }

  def start(): Unit = {
    println(methods)
    val system = ActorSystem("ConnectionSystem")

    val PORT = 9999
    val bind: java.net.ServerSocket = new ServerSocket(PORT)

    while (true) {
      val connection: java.net.Socket = bind.accept()
      val actor = system.actorOf(Props[ConnectionActor])
      actor ! connection
    }
  }
}
