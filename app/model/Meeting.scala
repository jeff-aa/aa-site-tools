package model

import java.util.Date
import play.api.db.DB
import anorm._
import play.api.Play.current
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.Logger

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

  def getMeetings(city: Option[String],
                  dayOfWeek: Option[Int],
                  lgbt: Option[Boolean],
                  closed: Option[Boolean],
                  young: Option[Boolean]) : List[Meeting] = {

    val cityConstraint = city map { x => "GROUPS.city = {city}" }
    val dayOfWeekConstraint = dayOfWeek map { x => "MEETING.DAY_OF_WEEK = {dayOfWeek}" }
    val closedConstraint = closed map { x => "GROUPS.CLOSED = {closed}" }
    val lgbtConstraint = lgbt map { x => "GROUPS.LGBT = {lgbt}" }
    val youngConstraint = young map { x => "GROUPS.YOUNG = {young}" }

    val joinConstraint = Some("GROUPS.ID = MEETING.GROUP_ID")

    val constraints : String = List(joinConstraint, cityConstraint, dayOfWeekConstraint, closedConstraint, lgbtConstraint, youngConstraint).flatten.mkString(" AND ")


    val params = List(city map { value => NamedParameter("city", ParameterValue.toParameterValue(value))},
      dayOfWeek map { value => NamedParameter("dayOfWeek", ParameterValue.toParameterValue(value))},
      lgbt map { value => NamedParameter("lgbt", ParameterValue.toParameterValue(value))},
      closed map { value => NamedParameter("closed", ParameterValue.toParameterValue(value))},
      young map { value => NamedParameter("young", ParameterValue.toParameterValue(value))}
    ).flatten

    DB.withConnection { implicit c =>
      var query : SimpleSql[Row] = SQL("""select GROUPS.ID, GROUPS.NAME, GROUPS.ADDRESS, GROUPS.CITY, GROUPS.LGBT, GROUPS.CLOSED, GROUPS.YOUNG, GROUPS.NOTES, MEETING.MEETING_ID, MEETING.DAY_OF_WEEK , MEETING.TIME_OF_DAY
            from groups, meeting
              where """ + constraints).on(params : _*)

      query().map { row =>
        val group = Group(Some(row[Long]("GROUPS.ID")),
          row[String]("GROUPS.NAME"),
          row[String]("GROUPS.ADDRESS"),
          row[String]("GROUPS.CITY"),
          row[Boolean]("GROUPS.LGBT"),
          row[Boolean]("GROUPS.CLOSED"),
          row[Boolean]("GROUPS.YOUNG"),
          row[String]("GROUPS.NOTES"))

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