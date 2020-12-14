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
    if (queryParams.contains("img")) {
      val decoded = Base64.getDecoder().decode(queryParams("img"))
      val img = new String(decoded, StandardCharsets.UTF_8)
      println(img)
      return img
    }
    else {
      throw new Exception("No image string found.");
    }
  }

  def setup(server: com.github.kkhan01.amniservo.Amniservo) = {
    server.add("/reverseText", reverseText)
    server.add("/log", log)
  }
}
