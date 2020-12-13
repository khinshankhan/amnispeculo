package com.github.kkhan01.amniscite

import com.github.kkhan01.amniservo.Amniservo

object Handler {
  def reverse(queryParams: Map[String, String]): String = {
    if (queryParams.contains("input")) {
      return queryParams("input").reverse
    }
    else {
      throw new Exception("No input string found.");
    }
  }

  def setup(server: com.github.kkhan01.amniservo.Amniservo) = {
    server.add("/reverse", reverse)
  }
}
