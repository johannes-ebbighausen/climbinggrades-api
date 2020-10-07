package de.vxr.mountainapi

import cats.effect.{IO, Sync}
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object MountainapiRoutes {

  def gradesRoutes[F[_]: Sync](G: Grades[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "grade" / name =>
        for {
          greeting <- G.translate(Grades.Grade(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }
}