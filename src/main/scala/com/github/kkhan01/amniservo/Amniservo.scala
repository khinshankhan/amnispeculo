package com.github.kkhan01.amniservo

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}

import com.github.kkhan01.amniservo.actors.ValidationActor

class Amniservo {
  var routes = Map[String, Map[String, (Map[String, String]) => String]]()

  def add(route: String, method: String, fn: (Map[String, String]) => String): Unit = {
    if (routes.contains(route)) {
      if (routes(route).contains(method)){
        throw new Exception("Duplicated route detected.")
      } else {
        routes = routes + (route -> (routes(route) + (method -> fn)))
      }
    }
    else {
      routes = routes + (route -> Map(method -> fn))
    }
  }

  def start(): Unit = {
    val system = ActorSystem("ConnectionSystem")

    val PORT = 9999
    val bind: ServerSocket = new ServerSocket(PORT)

    while (true) {
      val connection: Socket = bind.accept()
      val actor = system.actorOf(Props(classOf[ValidationActor], routes))
      actor ! connection
    }
  }
}
