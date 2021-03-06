package de.vxr.mountainapi

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.util.Properties

import scala.concurrent.ExecutionContext.global

object MountainapiServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val port: Int = Properties.envOrElse("PORT", "8080").toInt
    for {
      client <- BlazeClientBuilder[F](global).stream
//      helloWorldAlg = HelloWorld.impl[F]
//      jokeAlg = Jokes.impl[F](client)
      grades = Grades.impl[F]

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
//        MountainapiRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
//        MountainapiRoutes.jokeRoutes[F](jokeAlg) <+>
          MountainapiRoutes.gradesRoutes[F](grades)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)


      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
