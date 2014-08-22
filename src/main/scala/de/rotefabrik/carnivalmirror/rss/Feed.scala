package de.rotefabrik.carnivalmirror.rss

import scala.xml.{ XML, Elem, Node, NodeSeq }
import java.lang.reflect.Method
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class Feed(url:Option[String] = None, var stringContent:Option[String] = None, var xml:Option[Elem] = None, feedItemGenerator:FeedItemGenerator = new FeedItemGenerator()) {

	var items = new Array[FeedItem](0)

	def getFromUrl:Option[String] = {
		return None
	}

	def getStringContent:Option[String] = {
		if(!stringContent.nonEmpty) {
			stringContent = getFromUrl
		}
		return stringContent
	}

	def getXml:Option[Elem] = {
		if(!xml.nonEmpty) {
			if(stringContent.nonEmpty) {
				xml = Some(XML.loadString(stringContent.get))
			}
		}
		return xml
	}

	def getItems:Array[FeedItem] = {
		return getFeedItemGenerator(getXml).getItems
	}

	def getItem(index:Int):Option[FeedItem] = {
		return Some((getItems)(index))
	}

	private def getFeedItemGenerator(xml:Option[Elem]):FeedItemGenerator = {
		return new FeedItemGenerator(xml)
	} 

}


class FeedItem(var node:Option[Node] = None, var title:Option[String] = None, var link:Option[String] = None, var description:Option[String] = None, var creator:Option[String] = None, var categories:Option[Array[String]] = None) {

	def getTitle:Option[String] = {
		return getProperty("title")
	}

	def getLink:Option[String] = {
		return getProperty("link")
	}

	def getDescription:Option[String] = {
		return getProperty("description")
	}

	def getCreator:Option[String] = {
		return getProperty("creator", "creator", "dc")
	}

	def getCategories:Option[Array[String]] = {
		if(!categories.nonEmpty && node.nonEmpty) {
			val categoryNodes:NodeSeq = node.get \ "category"
			val tempCategories:Array[String] = categoryNodes.map(_.text).toArray
			categories = Some(tempCategories)
		}
		return categories
	}

	def getProperty(name:String):Option[String] = {
		return getProperty(name, name)
	}

	def getProperty(name:String, nodeName:String):Option[String] = {
		return getProperty(name, name, null)
	}

	def getProperty(name:String, nodeName:String, prefix:String):Option[String] = {
		val propertyMethod:Method = this.getClass.getMethod(name)
		if(propertyMethod == null) {
			return None
		}
		val propertyValue:Option[String] = propertyMethod.invoke(this).asInstanceOf[Option[String]]
		if(!propertyValue.nonEmpty && node.nonEmpty) {
			var nodeElements = (node.get \ nodeName).filter(_.prefix == prefix)
			if(nodeElements.length > 0) {
				this.getClass.getMethod(name + "_$eq", classOf[Option[_]]).invoke(this, Some(nodeElements(0).text))
			}
		} 
		return propertyMethod.invoke(this).asInstanceOf[Option[String]]
	}

	def getJson:JValue = {
		val json:JValue = JObject(
			"title" -> getTitle.getOrElse(null),
			"link" -> getLink.getOrElse(null),
			"description" -> getDescription.getOrElse(null),
			"creator" -> getCreator.getOrElse(null),
			"categories" -> getCategories.getOrElse(new Array[String](0)).toList
		)
		return json
	}

}


class FeedItemGenerator(var xml:Option[Elem] = None) {

	private val EmptyItemArray = new Array[FeedItem](0)
	
	def getItems:Array[FeedItem] = {
		if(!xml.nonEmpty) {
			return new Array[FeedItem](0)
		}
		return extractItemsFromRssFeed(xml.get)
	}

	private def extractItemsFromRssFeed(xmlFeed:Elem): Array[FeedItem] = {
		if(!hasChannel(xmlFeed)) {
			return EmptyItemArray
		}
		var itemElements = extraxtItemNodes(xmlFeed)
		var items = new Array[FeedItem](0)
		for(itemNode <- itemElements) {
			val item = buildFeedItem(itemNode)
			items = items :+ item
		}
		return items
	}

	private def hasChannel(xmlFeed:Elem): Boolean = {
		val result:NodeSeq = xmlFeed \\ "channel"
		return  result.length > 0
	}

	private def extraxtItemNodes(xmlFeed:Elem):NodeSeq = {
		return xmlFeed \\ "channel" \ "item"
	}

	private def buildFeedItem(itemNode:Node):FeedItem = {
		return new FeedItem(Some(itemNode))
	}

}