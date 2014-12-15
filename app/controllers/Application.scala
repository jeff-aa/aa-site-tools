package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import model.{Page, Group, Meeting}
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

  def showPage(id: String, activeClass: String) = Action {
    val page = Page.getPage(id)

    if(page.isDefined) {
      Ok(views.html.showPage("groupnamefixme", page.get.content, activeClass))
    } else {
      NotFound
    }
  }

  def index = showPage("homepage", "home")
  def literature = showPage("literature", "literature")
  def contact = showPage("contact", "contact")
  def about = showPage("about", "about")

  def meetingsToday = Action {
    val now = new DateTime()
    val dayOfWeek = now.dayOfWeek().get()

    searchInternal(None, Some(dayOfWeek), true, None, None, None)
  }

  def findMeeting = Action {
    val cities = Group.getDistinctCities()
    Ok(views.html.findMeetings(groupName, (new DateTime()).dayOfWeek.get, cities))
  }

  case class SearchForm(dayOfWeek: Int, city: String, lgbt: Option[Boolean], closed: Option[Boolean], young: Option[Boolean]) {
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
        "city" -> text,
        "lgbt" -> optional(boolean),
        "closed" -> optional(boolean),
        "young" -> optional(boolean)
      )(SearchForm.apply)(SearchForm.unapply)
    )

    val search = searchForm.bindFromRequest().get

    searchInternal(search.getCity(), search.getDayOfWeek(), false, search.lgbt, search.closed, search.young)
  }

  def searchInternal(city: Option[String], dayOfWeek: Option[Int], meetingToday: Boolean, lgbt: Option[Boolean], closed: Option[Boolean], young: Option[Boolean]) : Result = {
    val meetings : List[Meeting] = Meeting.getMeetings(city, dayOfWeek, lgbt, closed, young)
    val meetingsByCity = meetings.groupBy(_.group.city)

    Ok(views.html.meetingsView.render("Spuzzum AA", getDateString(dayOfWeek), None, meetingsByCity, meetingToday))
  }
}