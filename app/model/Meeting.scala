package model

import java.util.Date
import play.api.db.DB
import anorm._
import play.api.Play.current

/**
 * Created by jwright on 2014-11-28.
 */

object Meeting {
  def save(meeting: Meeting)  {
    val savedId : Option[Long] = DB.withConnection { implicit c =>
      SQL("INSERT into MEETING(GROUP_ID, DAY_OF_WEEK, TIME_OF_DAY) values({GROUP_ID}, {DAY_OF_WEEK}, {TIME_OF_DAY})")
        .on("GROUP_ID" -> meeting.groupId, "DAY_OF_WEEK" -> meeting.dayOfWeek, "TIME_OF_DAY" -> meeting.timeOfDay)
        .executeInsert()
    }

    Meeting(savedId, meeting.groupId, meeting.dayOfWeek, meeting.timeOfDay)
  }
}

case class Meeting(meetingID: Option[Long], groupId: Long, dayOfWeek: Int, timeOfDay: Date);