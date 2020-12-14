package com.github.kkhan01.amniscite

import java.util.Base64
import java.nio.charset.StandardCharsets

import com.github.kkhan01.amniservo.Amniservo

object Handler {
  def reverseText(queryParams: Map[String, String]): String = {
    if (queryParams.contains("input")) {
      return queryParams("input").reverse
    }
    else {
      throw new Exception("No input string found.");
    }
  }

  def log(queryParams: Map[String, String]): String = {
    val decoded = Base64.getDecoder().decode(queryParams("img"))
    val str = new String(decoded, StandardCharsets.UTF_8)
    println(str)
    return str
  }

  def setup(server: com.github.kkhan01.amniservo.Amniservo) = {
    server.add("/reverseText", reverseText)
    server.add("/log", log)
  }
}
