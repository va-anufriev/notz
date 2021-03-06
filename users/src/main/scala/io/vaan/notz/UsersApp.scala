package io.vaan.notz

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.AkkaManagement
import io.vaan.notz.users.{UserActor, UserRegistry, UserRoutes}

import scala.util.{Failure, Success}

object UsersApp {
  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val port = 8080
    val futureBinding = Http().bindAndHandle(routes, "localhost", port)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      UserActor.initSharding(context.system)

      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      val routes = new UserRoutes(userRegistryActor)(context.system)
      startHttpServer(routes.usersRoutes, context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "notz-users")
    AkkaManagement(system).start()
  }
}

