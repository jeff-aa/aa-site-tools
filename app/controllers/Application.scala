package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.Meeting

object Application extends Controller {



  def index = Action {
    Ok(views.html.index("Scuzzum AA"))
  }

  def meetingsToday = Action {

    val meetings : List[Meeting] = Meeting.getMeetingsToday()
    val meetingsByCity = meetings.groupBy(_.group.city)

    Ok(views.html.meetingToday.render("Spuzzum AA", meetingsByCity))
  }
}