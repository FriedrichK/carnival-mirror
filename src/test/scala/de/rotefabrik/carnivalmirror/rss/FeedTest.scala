package de.rotefabrik.carnivalmirror.rss

import scala.io.Source

import scala.xml.{ XML, Elem }

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import org.scalatest._
import Matchers._
import org.scalatest.FunSuite
 
class FeedSuite extends FunSuite {

	val SecondItemTitle = "Ex-Governor to Return to Stand in Virginia Corruption Trial"
	val SecondItemLink = "http://rss.nytimes.com/c/34625/f/640350/s/3db83865/sc/1/l/0L0Snytimes0N0C20A140C0A80C220Cus0Cpolitics0Ctrial0Eof0Eformer0Evirginia0Egovernor0Ebob0Emcdonnell0Bhtml0Dpartner0Frss0Gemc0Frss/story01.htm"
	val SecondItemCreator = "By JONATHAN WEISMAN and JENNIFER STEINHAUER"
	val NyTimesUsXml = "/rss_feeds/2014-08-21_nytimes-us.xml"
 
 	test("test returns expected second item") {
		val xmlContentFromResource:String = getXmlContentFromResource(NyTimesUsXml)
		val feed = new Feed(stringContent = Some(xmlContentFromResource))

		val actual:Option[FeedItem] = feed.getItem(1)

		actual.get.getTitle.get should equal (SecondItemTitle)
		actual.get.getLink.get should equal (SecondItemLink)
		actual.get.getCreator.get should equal (SecondItemCreator)
	}

	test("FeedItem generator should return expected number of items") {
		var xml:Elem = XML.loadString(getXmlContentFromResource(NyTimesUsXml))
		var generator:FeedItemGenerator = new FeedItemGenerator(Some(xml))

		var actual:Array[FeedItem] = generator.getItems

		actual.length should equal (25)
	}

	test("Feeditem should parse to JSON as expected") {
		val categories:Array[String] = Array("one", "two", "three")
		val feedItem = new FeedItem(title = Some("testTitle"), description = Some("testDescription"), creator = Some("testCreator"), categories = Some(categories))

		val actual = feedItem.getJson

		println(compact(render(actual)))
		compact(render(actual)) should equal ("{\"title\":\"testTitle\",\"link\":null,\"description\":\"testDescription\",\"creator\":\"testCreator\",\"categories\":[\"one\",\"two\",\"three\"]}")
	}

	def getXmlContentFromResource(uri:String): String = {
		val source = Source.fromURL(getClass.getResource(uri))
		val lines = source.mkString
		source.close()
		return lines
	}

}