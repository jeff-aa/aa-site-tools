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
      SQL("INSERT into GROUPS(NAME, ADDRESS, CITY) values({NAME}, {ADDRESS}, {CITY})")
        .on("NAME" -> group.name, "ADDRESS" -> group.address, "CITY" -> group.city)
        .executeInsert()
    }

    Group(savedId, group.name, group.address, group.city)
  }
}

case class Group(id: Option[Long], name: String, address: String, city: String);
