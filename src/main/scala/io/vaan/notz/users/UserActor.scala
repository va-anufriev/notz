package io.vaan.notz.users

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.vaan.notz.users.model.User

object UserActor {
  // commands and events
  sealed trait Command
  sealed trait Event

  final case class ChangeFirstName(firstName: String) extends Command
  final case class ChangeLastName(lastName: String) extends Command

  final case class ChangedFirstName(firstName: String) extends Event
  final case class ChangedLastName(lastName: String) extends Event

  // state
  final case class UserState(
    firstName: String,
    lastName: String,
    email: String
  )

  // empty state constructor
  object UserState {
    def apply(user: User): UserState = new UserState(
      user.firstName,
      user.lastName,
      user.email
    )
  }

  val commandHandler: (UserState, Command) => Effect[Event, UserState] = { (state, command) =>
    command match {
      case ChangeFirstName(firstName) => Effect.persist(ChangedFirstName(firstName))
      case ChangeLastName(lastName) => throw new NotImplementedError
    }
  }

  val eventHandler: (UserState, Event) => UserState = { (state, event) =>
    event match {
      case ChangedFirstName(firstName) => state.copy(firstName = firstName)
      case ChangedLastName(lastName) => throw new NotImplementedError
    }
  }

  def apply(user: User): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, UserState](
      persistenceId = PersistenceId.ofUniqueId(user.email),
      emptyState = UserState(user),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
  }
}