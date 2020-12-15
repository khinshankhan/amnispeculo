package com.github.kkhan01.amniscite

import java.util.Base64
import java.nio.charset.StandardCharsets
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import java.awt.geom.AffineTransform
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.nio.file.{Paths, Files}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import akka.stream.scaladsl.{ Flow, Sink, Source }

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.kkhan01.amniservo.Amniservo

object Handler {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def reverseText(params: Map[String, String]): String = {
    if (params.contains("input")) {
      return params("input").reverse
    }
    else {
      throw new Exception("No input string found.");
    }
  }

  def log(params: Map[String, String]): String = {
    val decoded = Base64.getDecoder().decode(params("img"))
    val str = new String(decoded, StandardCharsets.UTF_8)
    println(str)
    return str
  }

  def rotate_180 = Flow[BufferedImage].map(
    img => {
      val width = img.getWidth()
      val height = img.getHeight()
      val at = AffineTransform.getRotateInstance(Math.PI, width/2, height/2)
      val ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      val g = ret.createGraphics()
      g.transform(at)
      g.drawImage(img, 0, 0, null)
      g.dispose()
      ret
    })

  def grayscale = Flow[BufferedImage].map(
    img => {
      val w = img.getWidth
      val h = img.getHeight
      for { w1 <- (0 until w).toVector
            h1 <- (0 until h).toVector
      } yield {
        val col = img.getRGB(w1, h1)
        val red =  (col & 0xff0000) / 65536
        val green = (col & 0xff00) / 256
        val blue = (col & 0xff)
        val graycol = (red + green + blue) / 3
        img.setRGB(w1, h1, new Color(graycol, graycol, graycol).getRGB)
      }
      img
    })

  def toBase64 = Flow[BufferedImage].map(
    img => {
      val os = new ByteArrayOutputStream
      val b64 = Base64.getEncoder.wrap(os)
      ImageIO.write(img, "png", b64)
      val encoded = os.toString("UTF-8")
      encoded
    }
  )

  val to_string = Sink.fold[String, String]("")(_ + _)

  def processImage(image: BufferedImage): Future[String] = Source
    .single(image)
    .via(grayscale)
    .via(toBase64)
    .runWith(to_string)

  def imager(params: Map[String, String]): String = {
    if (!params.contains("filename")){
      return "No filename provided."
    }
    val filename = params("filename")
    // TODO: add more file support
    if (!Files.exists(Paths.get(filename)) || !filename.endsWith(".png")){
      return "Invalid file."
    }

    val imageParams = params - "filename"

    val image: BufferedImage = ImageIO.read(new File(filename));
    val processed = processImage(image)
    val base64Image = Await.result(processed, 30 second)
    val res = s"""<html><body><img src="data:image/png;base64,${base64Image}" alt="image"></body></html>"""
    return res
  }

  def setup(server: com.github.kkhan01.amniservo.Amniservo) = {
    server.add("/reverseText", reverseText)
    server.add("/log", log)
    server.add("/imager", imager)
  }
}
