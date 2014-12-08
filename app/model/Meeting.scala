package model

import java.util.Date
import play.api.db.DB
import anorm._
import play.api.Play.current
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by jwright on 2014-11-28.
 */

object Meeting {
  def save(meeting: Meeting)  {
    val savedId : Option[Long] = DB.withConnection { implicit c =>
      SQL("INSERT into MEETING(GROUP_ID, DAY_OF_WEEK, TIME_OF_DAY) values({GROUP_ID}, {DAY_OF_WEEK}, {TIME_OF_DAY})")
        .on("GROUP_ID" -> meeting.group.id, "DAY_OF_WEEK" -> meeting.dayOfWeek, "TIME_OF_DAY" -> meeting.timeOfDay)
        .executeInsert()
    }

    Meeting(savedId, meeting.group, meeting.dayOfWeek, meeting.timeOfDay)
  }


  def getMeetings(city: Option[String], dayOfWeek: Option[Int]) : List[Meeting] = {

    val cityConstraint = city match {
      case None => None
      case Some(city) =>
        Some("GROUPS.city = {city}")
    }

    val dayOfWeekConstraint = dayOfWeek match {
      case None => None
      case Some(day) =>
        Some("MEETING.DAY_OF_WEEK = {dayOfWeek}")
    }

    val joinConstraint = Some("GROUPS.ID = MEETING.GROUP_ID")

    val constraints : String = List(cityConstraint, dayOfWeekConstraint, joinConstraint).flatten.mkString(" AND ")

    DB.withConnection { implicit c =>
      val query = SQL("""select GROUPS.ID, GROUPS.NAME, GROUPS.ADDRESS, GROUPS.CITY, MEETING.MEETING_ID, MEETING.DAY_OF_WEEK , MEETING.TIME_OF_DAY
            from groups, meeting
              where """ + constraints)
        .on("dayOfWeek" -> dayOfWeek)

      var completeQuery = if(city.isDefined) {
        query.on("city" -> city.get)
      } else {
        query
      }

      completeQuery = if(dayOfWeek.isDefined) {
        query.on("dayOfWeek" -> dayOfWeek.get)
      } else {
        query
      }

      completeQuery().map { row =>
        val group = Group(Some(row[Long]("GROUPS.ID")), row[String]("GROUPS.NAME"), row[String]("GROUPS.ADDRESS"), row[String]("GROUPS.CITY"))
        Meeting(Some(row[Long]("MEETING.MEETING_ID")), group, row[Int]("MEETING.DAY_OF_WEEK"), row[Date]("MEETING.TIME_OF_DAY"))
      }.toList
    }
  }
}

case class Meeting(meetingID: Option[Long], group: Group, dayOfWeek: Int, timeOfDay: Date) {
  def getGoogleMapLink() : String = {
    "https://www.google.com/maps/place/" + group.address.replaceAll("\n", " ") + ", " + group.city
  }

  val dayOfWeekFormatter = DateTimeFormat.forPattern("EEEE")

  def getDayOfWeekPretty() : String = {
    dayOfWeekFormatter.print(new DateTime().withDayOfWeek(dayOfWeek))
  }

  val timeFormatter  = DateTimeFormat.forPattern("h:mm aa")

  def getTimePretty() : String = {
    timeFormatter.print(new DateTime(timeOfDay))
  }
}