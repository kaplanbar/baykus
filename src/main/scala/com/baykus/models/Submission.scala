package com.baykus.models

import java.nio.file.{Path, Paths}
import de.jplag.options.LanguageOption
import spray.json._

import java.util.{Base64, UUID}

final case class Submission(
    contestId: ContestId,
    sourceCode: SourceCode,
    user: User,
    submissionId: SubmissionId,
    language: LanguageOption
) {
  private def fileName = {
    val extension = language match {
      case LanguageOption.C_CPP    => s"cpp"
      case LanguageOption.JAVA     => s"java"
      case LanguageOption.PYTHON_3 => s"py"
    }
    s"${submissionId.value}.$extension"
  }
  def path(rootDir: Path): Path =
    Paths.get(ContestId.directoryPath(rootDir, contestId).toString, s"$fileName")
}

object Submission {
  object JsonProtocol extends DefaultJsonProtocol {
    // TODO: Move these formats to another package
    implicit val UUIDJsonFormat: RootJsonFormat[UUID] = new RootJsonFormat[UUID] {
      override def read(json: JsValue): UUID = UUID.fromString(json.convertTo[String])
      override def write(obj: UUID): JsValue = obj.toString.toJson
    }

    implicit val ContestIdJsonFormat: RootJsonFormat[ContestId] = new RootJsonFormat[ContestId] {
      override def read(json: JsValue): ContestId = ContestId(json.convertTo[UUID])
      override def write(obj: ContestId): JsValue = obj.value.toJson
    }

    implicit val SourceCodeJsonFormat: RootJsonFormat[SourceCode] = new RootJsonFormat[SourceCode] {
      override def read(json: JsValue): SourceCode = SourceCode(Base64.getDecoder.decode(json.convertTo[String]).map(_.toChar).mkString)
      override def write(obj: SourceCode): JsValue = Base64.getEncoder.encodeToString(obj.value.getBytes).toJson
    }

    implicit val UserJsonFormat: RootJsonFormat[User] = new RootJsonFormat[User] {
      override def read(json: JsValue): User = User(json.convertTo[String])
      override def write(obj: User): JsValue = obj.value.toJson
    }

    implicit val SubmissionIdJsonFormat: RootJsonFormat[SubmissionId] = new RootJsonFormat[SubmissionId] {
      override def read(json: JsValue): SubmissionId = SubmissionId(json.convertTo[UUID])
      override def write(obj: SubmissionId): JsValue = obj.value.toJson
    }

    implicit val LanguageJsonFormat: RootJsonFormat[LanguageOption] = new RootJsonFormat[LanguageOption] {
      override def read(json: JsValue): LanguageOption = json.convertTo[String] match {
        case l if l == LanguageOption.C_CPP.getDisplayName    => LanguageOption.C_CPP
        case l if l == LanguageOption.JAVA.getDisplayName     => LanguageOption.JAVA
        case l if l == LanguageOption.PYTHON_3.getDisplayName => LanguageOption.PYTHON_3
        case _                                                => deserializationError("Unsupported language")
      }
      override def write(obj: LanguageOption): JsValue = obj.getDisplayName.toJson
    }

    implicit val SubmissionJsonFormat: RootJsonFormat[Submission] =
      jsonFormat(Submission.apply, "contest_id", "source_code", "user", "submission_id", "language")
  }
}
