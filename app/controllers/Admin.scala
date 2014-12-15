package controllers

import play.api.mvc.{Results, Action, Controller}
import play.api.db._
import play.api.data.Form
import play.api.data.Forms._
import model._
import play.api.Logger
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import scala.util.Try
import play.mvc.Http

/**
 * Created by jwright on 2014-11-27.
 */
object Admin extends Controller  {

  def admin = Action {
    Ok(views.html.admin(SiteSettings("placeholder"), "testUser"))
  }

  def addMeetingPage = Action {
    val cities = Group.getDistinctCities()
    Ok(views.html.addMeeting("derp", cities))
  }

  def addMeetingPageDifferent = Action {
    val cities = Group.getDistinctCities()
    Ok(views.html.differentTimes("derp", cities))
  }

  def editPage(id: String) = Action { request =>
    val page = Page.getPage(id)

    val savedString : String = request.getQueryString("saved").getOrElse("false")
    val saved : Boolean = Try(savedString.toBoolean).getOrElse(false)

    if(page.isDefined) {
      Ok(views.html.edit(page.get.content, id, saved))
    } else {
      Ok(views.html.edit("Insert content here.", id, saved))
    }
  }

  def savePage(id: String) = Action { implicit request =>
    val savePage = Form(
      "content" -> text
    )

    val (content) = savePage.bindFromRequest().get

    Page.savePage(id, content)

    Results.Redirect(routes.Admin.editPage(id).url, Map("saved" -> List("true")), Http.Status.MOVED_PERMANENTLY)
  }

  def saveMeeting = Action { implicit request =>
    Logger.error(request.body.toString)

    val loginForm = Form(
      tuple(
        "meeting-name" -> text,
        "city" -> text,
        "address" -> text,
        "meeting-time" -> text,
        "SUNDAY" -> optional(text),
        "MONDAY" -> optional(text),
        "TUESDAY" -> optional(text),
        "WEDNESDAY" -> optional(text),
        "THURSDAY" -> optional(text),
        "FRIDAY" -> optional(text),
        "SATURDAY" -> optional(text),
        "lgbt" -> optional(boolean),
        "closed" -> optional(boolean),
        "young" -> optional(boolean),
        "notes" -> optional(text)
      )
    )

    val (meetingName,
      city,
      address,
      timeString,
      sunday,
      monday,
      tuesday,
      wednesday,
      thursday,
      friday,
      saturday,
      lgbt,
      closed,
      young,
      notes) = loginForm.bindFromRequest.get

    val group = Group.save(Group(None, meetingName, address, city, lgbt.getOrElse(false), closed.getOrElse(false), young.getOrElse(false), notes.getOrElse("")))

    val daysOfWeek = 1 to 7
    val meetsOnDay : List[(Boolean, Int)] = List(sunday, monday, tuesday, wednesday, thursday, friday, saturday) map { _.getOrElse("false").toBoolean } zip daysOfWeek

    val formatter = DateTimeFormat.forPattern("hh:mm a")
    val time = formatter.parseDateTime(timeString).toDate

    val meetings : List[Meeting] = meetsOnDay
        .filter({ x: (Boolean, Int) => x._1 })
        .map({ x: (Boolean, Int) => Meeting(None, group, x._2, time) })

    meetings map { Meeting.save(_) }

    Ok("derp")
  }
}
