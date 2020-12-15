package com.github.kkhan01.amniscite

import java.util.Base64
import java.nio.charset.StandardCharsets
import java.awt.image.BufferedImage
import java.io.{File, IOException, ByteArrayOutputStream}
import javax.imageio.ImageIO
import java.awt.geom.AffineTransform
import java.awt.Color
import java.nio.file.{Paths, Files}
import java.net.URL;

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import akka.stream.scaladsl.{ Flow, Sink, Source }

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.kkhan01.amniservo.Amniservo

object Handler {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val ImageLookup = Map("flip_vertical" -> flip_vertical, "flip_horizontal" -> flip_horizontal, "rotate_180" -> rotate_180, "rotate_90" -> rotate_90, "grayscale" -> grayscale, "invert" -> invert)

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

  def chat(params: Map[String, String]): String = {
    if (params.contains("input")) {
      return "SERVER:" + params("input")
    }
    else {
      throw new Exception("No input string found.");
    }
  }

  def chatReverse(params: Map[String, String]): String = {
    if (params.contains("input")) {
      return "SERVER:" + params("input").reverse
    }
    else {
      throw new Exception("No input string found.");
    }
  }


  def flip_vertical = Flow[BufferedImage].map(
    img => {
      val width = img.getWidth()
      val height = img.getHeight()
      val ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      val g = ret.createGraphics()
      g.drawImage(img, 0, 0, width, height, 0, height, width, 0, null);
      g.dispose()
      ret
    })

  def flip_horizontal = Flow[BufferedImage].map(
    img => {
      val width = img.getWidth()
      val height = img.getHeight()
      val ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      val g = ret.createGraphics()
      g.drawImage(img, 0, 0, width, height, width, 0, 0, height, null);
      g.dispose()
      ret
    })

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

  def rotate_90 = Flow[BufferedImage].map(
    img => {
      val width = img.getWidth()
      val height = img.getHeight()
      val at = new AffineTransform()
      at.rotate(Math.PI/2, width/2, height/2)
      val offset = (width-height)/2
      at.translate(offset, offset)
      val ret = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB)
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
        val rgba = img.getRGB(w1, h1)
        val col = new Color(rgba, true)
        val red =  col.getRed()
        val green = col.getGreen()
        val blue = col.getBlue()
        val graycol = (red + green + blue) / 3
        img.setRGB(w1, h1, new Color(graycol, graycol, graycol).getRGB)
      }
      img
    })

  def invert = Flow[BufferedImage].map(
    img => {
      val w = img.getWidth
      val h = img.getHeight
      for { w1 <- (0 until w).toVector
            h1 <- (0 until h).toVector
      } yield {
        val rgba = img.getRGB(w1, h1)
        val col = new Color(rgba, true)
        val red =  255 - col.getRed()
        val green = 255 - col.getGreen()
        val blue = 255 - col.getBlue()
        img.setRGB(w1, h1, new Color(red, green, blue).getRGB)
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

  def processImage(image: BufferedImage, flow: Flow[BufferedImage,String,akka.NotUsed]): Future[String] = Source
    .single(image)
    .via(flow)
    .runWith(to_string)

  def imageToHTML(image: BufferedImage, oplist: String): String = {
    val end_flow = if (oplist != "") oplist.split(",").foldRight(toBase64){
      (str, combined_flow) => ImageLookup.get(str).get.via(combined_flow)
    } else toBase64

    val processed = processImage(image, end_flow)
    val base64Image = Await.result(processed, 30 second)
    val res = s"""<html><body><img src="data:image/png;base64,${base64Image}" alt="image"></body></html>"""
    res
  }

  def image_file(params: Map[String, String]): String = {
    if (!params.contains("filename")){
      return "No filename provided."
    }

    val filename = params("filename")
    if (!Files.exists(Paths.get(filename))){
      return "file doesn't exist"
    }

    val oplist = params.getOrElse("oplist", "")

    try {
      val image = ImageIO.read(new File(filename))
      imageToHTML(image, oplist)
    } catch {
      case err: Throwable => "invalid image"
    }
  }

  def image_link(params: Map[String, String]): String = {
    if (!params.contains("link")){
      return "No link provided."
    }

    val link = params("link")
    val oplist = params.getOrElse("oplist", "")

    try {
      val image = ImageIO.read(new URL(link))
      imageToHTML(image, oplist)
    } catch {
      case err: Throwable => "invalid image"
    }
  }

  def front_end_file(params: Map[String, String]): String = {
    io.Source.fromFile("client/file.html").mkString
  }

  def front_end_link(params: Map[String, String]): String = {
    io.Source.fromFile("client/link.html").mkString
  }

  def setup(server: com.github.kkhan01.amniservo.Amniservo) = {
    server.add("/reverseText", "GET", reverseText)
    server.add("/log", "POST", log)
    server.add("/chat", "STREAM", chat)
    server.add("/chatReverse", "STREAM", chatReverse)
    server.add("/image_file", "GET", image_file)
    server.add("/image_link", "GET", image_link)
    server.add("/file", "GET", front_end_file)
    server.add("/link", "GET", front_end_link)
  }
}
