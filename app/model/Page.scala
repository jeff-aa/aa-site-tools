package model

import java.util.Date
import play.api.db.DB
import anorm._
import play.api.Play.current

/**
 * Created by jwright on 2014-12-14.
 */
case class Page(id: String, content: String, lastModified: Date);

object Page {
  def getPage(id: String) : Option[Page] = {
    DB.withConnection { implicit c =>
      val query = SQL("SELECT ID, CONTENT, LASTMODIFIED from PAGES where ID={id}").on("id" -> id)
      val results = query()

      if(results.length == 1) {
        val result = results.head
        Some(Page(result[String]("ID"), result[String]("CONTENT"), result[Date]("LASTMODIFIED")))
      } else {
        None
      }
    }
  }

  def savePage(id: String, content: String) = {
    DB.withConnection { implicit c =>
      val query = SQL("MERGE INTO PAGES (ID, CONTENT, LASTMODIFIED) VALUES ({id}, {content}, {lastModified}) ")
        .on("id" -> id, "content" -> content, "lastModified" -> new Date())

      query.executeInsert()
    }
  }
}
