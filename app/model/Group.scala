package model

import play.api.db.DB
import anorm._
import scala.Some
import play.api.Play.current

/**
 * Created by jwright on 2014-11-28.
 */

object Group {
  def save(group: Group) : Group = {
    val savedId : Option[Long] = DB.withConnection { implicit c =>
      SQL("INSERT into GROUPS(NAME, ADDRESS, CITY, LGBT, CLOSED, YOUNG, NOTES) " +
        " values({NAME}, {ADDRESS}, {CITY}, {LGBT}, {CLOSED}, {YOUNG}, {NOTES})")
        .on("NAME" -> group.name,
            "ADDRESS" -> group.address,
            "CITY" -> group.city,
            "LGBT" -> group.lgbt,
            "CLOSED" -> group.closed,
            "YOUNG" -> group.young,
            "NOTES" -> group.notes)
        .executeInsert()
    }

    Group(savedId, group.name, group.address, group.city, group.lgbt, group.closed, group.young, group.notes)
  }

  def getDistinctCities() : Seq[String] = {
    DB.withConnection { implicit c =>
      val query = SQL("select distinct city  from groups")
      query().map(row => row[String]("city")).toList
    }
  }
}

case class Group(id: Option[Long], name: String, address: String, city: String, lgbt: Boolean, closed: Boolean, young: Boolean, notes: String);
