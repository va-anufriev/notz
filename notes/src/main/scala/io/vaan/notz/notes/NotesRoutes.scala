package io.vaan.notz.notes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class NotesRoutes()(implicit val system: ActorSystem[_]) {
  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("notes.routes.ask-timeout"))

  def getNotes: Future[List[Note]] = Future(List())

  private val notes = pathEnd {
    concat(
//      get {
//        complete(getNotes)
//      }
    )
  }

  val notesRoutes: Route =
    concat {
      pathPrefix("users") {
        notes
      }
    }
}
