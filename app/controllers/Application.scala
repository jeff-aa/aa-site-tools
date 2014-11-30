package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.Meeting
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Application extends Controller {

  private def getToday() : String = {
    val now = new DateTime()
    val formatter = DateTimeFormat.forPattern("EEEE, MMMM d, yyyy")
    formatter.print(now)
  }

  def index = Action {
    Ok(views.html.index("Scuzzum AA"))
  }

  def meetingsToday = Action {

    val meetings : List[Meeting] = Meeting.getMeetingsToday()
    val meetingsByCity = meetings.groupBy(_.group.city)

    Ok(views.html.meetingToday.render("Spuzzum AA", getToday(), meetingsByCity))
  }
}