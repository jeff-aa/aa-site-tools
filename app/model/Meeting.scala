package model

import java.util.Date
import play.api.db.DB
import anorm._
import play.api.Play.current
import org.joda.time.DateTime

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

  def getMeetingsToday() : List[Meeting] = {
    val now = new DateTime()
    val dayOfWeek = now.dayOfWeek().get()

    DB.withConnection { implicit c =>
      val query = SQL("""select GROUPS.ID, GROUPS.NAME, GROUPS.ADDRESS, GROUPS.CITY, MEETING.MEETING_ID, MEETING.DAY_OF_WEEK , MEETING.TIME_OF_DAY
            from groups, meeting
            where MEETING.DAY_OF_WEEK = {dayOfWeek} and GROUPS.ID = MEETING.GROUP_ID;""")
        .on("dayOfWeek" -> dayOfWeek)

      query().map { row =>
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
}