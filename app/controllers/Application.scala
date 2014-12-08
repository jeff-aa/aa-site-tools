package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.{Group, Meeting}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object Application extends Controller {

  val groupName: String = "Scuzzum AA" // TODO(jeff): add to DB

  private def getDateString(dayOfWeek: Option[Int]) : String = {
    val now = new DateTime()
    val fullFormatter = DateTimeFormat.forPattern("EEEE")

    if(dayOfWeek.isDefined) {
      fullFormatter.print(now.withDayOfWeek(dayOfWeek.get))
    } else {
      val weekStart = now.withDayOfWeek(1)
      val weekEnd = now.withDayOfWeek(7)

      val formatterStart = DateTimeFormat.forPattern("EEEE")
      formatterStart.print(weekStart) + " to "  + fullFormatter.print(weekEnd)
    }
  }

  def index = Action {
    Ok(views.html.index(groupName))
  }

  def meetingsToday = Action {
    val now = new DateTime()
    val dayOfWeek = now.dayOfWeek().get()

    searchInternal(None, Some(dayOfWeek), true)
  }

  def findMeeting = Action {
    val cities = Group.getDistinctCities()
    Ok(views.html.findMeetings(groupName, (new DateTime()).dayOfWeek.get, cities))
  }

  case class SearchForm(dayOfWeek: Int, city: String) {
    def getDayOfWeek() : Option[Int] = {
      if(dayOfWeek == -1) {
        None
      } else {
        Some(dayOfWeek)
      }
    }

    def getCity() : Option[String] = {
      if(city.equalsIgnoreCase("_all")) {
        None
      } else {
        Some(city)
      }
    }
  }

  def search = Action { implicit request =>
    val searchForm = Form(
      mapping(
        "dayOfWeek" -> number,
        "city" -> text
      )(SearchForm.apply)(SearchForm.unapply)
    )

    val search = searchForm.bindFromRequest().get

    searchInternal(search.getCity(), search.getDayOfWeek(), false)
  }

  def searchInternal(city: Option[String], dayOfWeek: Option[Int], meetingToday: Boolean) : Result = {
    val meetings : List[Meeting] = Meeting.getMeetings(city, dayOfWeek)
    val meetingsByCity = meetings.groupBy(_.group.city)

    Ok(views.html.meetingsView.render("Spuzzum AA", getDateString(dayOfWeek), None, meetingsByCity, meetingToday))
  }
}