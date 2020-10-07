package de.vxr.mountainapi

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait Grades[F[_]]{
  def translate(n: Grades.Grade): F[Grades.TranslatedGrade]
}

object Grades {

  private val gradesFU  = Map(
    "5-" -> "5a",
    "5"  -> "5a",
    "5+" -> "5b",
    "6-" -> "5b",
    "6"  -> "5c",
    "6+" -> "6a",
    "7-" -> "6a+",
    "7"  -> "6b",
    "7+" -> "6b+",
    "8-" -> "6c",
    "8-" -> "6c /6c+",
    "8"  -> "7a",
    "8+" -> "7a+",
    "9-" -> "7b / 7b+"
  )

  private val gradesUF = gradesFU.map {
    case (f, u) => (u, f)
  }

  implicit def apply[F[_]](implicit ev: Grades[F]): Grades[F] = ev

  final case class Grade(grade: String) extends AnyVal


  final case class TranslatedGrade(grade: String) extends AnyVal
  object TranslatedGrade {
    implicit val greetingEncoder: Encoder[TranslatedGrade] = new Encoder[TranslatedGrade] {
      final def apply(a: TranslatedGrade): Json = Json.obj(
        ("translatedGrade", Json.fromString(a.grade)),
      )
    }
    implicit def translatedGradeEntityEncoder[F[_]: Applicative]: EntityEncoder[F, TranslatedGrade] =
      jsonEncoderOf[F, TranslatedGrade]
  }

  def impl[F[_]: Applicative]: Grades[F] = new Grades[F]{
    def translate(n: Grades.Grade): F[Grades.TranslatedGrade] =
        TranslatedGrade(  gradesFU.getOrElse(n.grade, gradesUF.getOrElse(n.grade,"Ask Adam!"))).pure[F]
  }
}