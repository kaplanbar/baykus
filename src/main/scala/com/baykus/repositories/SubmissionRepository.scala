package com.baykus.repositories

import better.files.File
import com.baykus.models.ComparisonReport.UserInfo
import com.baykus.models._
import com.baykus.repositories.SubmissionRepository.{createFile, getFile}
import com.typesafe.config.Config
import de.jplag.options.LanguageOption
import org.jsoup.Jsoup

import java.nio.file.Path
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SubmissionRepository(config: Config)(implicit ec: ExecutionContext) {
  private val root: File = File(config.getString("plagiarism-checker-service.root-dir"))
    .createIfNotExists(asDirectory = true)

  def saveSubmission(submission: Submission): Future[Unit] =
    Future(
      createFile(submission.contestId, submission.submissionId, submission.language, root)
        .write(submission.sourceCode.value)
    )

  def getSourceCode(contestId: ContestId, submissionId: SubmissionId): Future[Option[SourceCode]] =
    Future(
      getFile(contestId, submissionId, root)
        .map(_.contentAsString)
        .map(SourceCode)
    )

  def reportPath(contestId: ContestId): Path     = (root / contestId.value.toString / "reports").path
  def sourceCodePath(contestId: ContestId): Path = (root / contestId.value.toString).path

  def clearReports(contestId: ContestId): Future[Unit] =
    Future(File(reportPath(contestId)).list.foreach(_.delete()))

  private def fileToComparisonReport(reportFile: File): ComparisonReport = {
    val extensionToLanguage = (e: String) =>
      e match {
        case "java" => LanguageOption.JAVA
        case "cpp"  => LanguageOption.C_CPP
        case "py"   => LanguageOption.PYTHON_3
        case _      => throw new Exception(s"Report file ${reportFile.name} did not have an valid extension")
    }
    val jsoup    = Jsoup.parse(reportFile.toJava)
    val title    = jsoup.title()
    val regex    = raw"Matches for ([0-9a-z-]*)\.[a-z]* & ([0-9a-z-]*)\.([a-z]*)".r
    val res      = regex.findFirstMatchIn(title).get
    val user1    = User(res.group(1))
    val user2    = User(res.group(2))
    val language = extensionToLanguage(res.group(3))
    jsoup.select("a").forEach(_.remove())
    val similarity   = (jsoup.select("h1").first().html.dropRight(1)).toDouble
    val codeSelector = jsoup.select("pre")
    ComparisonReport(
      UUID.randomUUID(),
      UserInfo(user1, s"<div>${codeSelector.first().toString}</div>"),
      UserInfo(user2, s"<div>${codeSelector.last().toString}</div>"),
      similarity,
      language
    )
  }

  def comparisonReports(contestId: ContestId): Future[Seq[ComparisonReport]] = Future {
    val (requiredFiles, extraFiles) = File(reportPath(contestId)).list.partition { file =>
      file.name.contains("match") && file.extension.contains(".html")
    }
    extraFiles.foreach(_.delete())
    val reports = requiredFiles.map(fileToComparisonReport).toSeq
    reports.foreach(saveComparisonReport)
    reports
  }

  def saveComparisonReport(comparisonReport: ComparisonReport): Unit = {
    val path = root / "reports" / s"${comparisonReport.comparisonReportId.toString}.bin"
    path.writeSerialized(comparisonReport.asInstanceOf[Serializable])
  }

  def getComparisonReport(comparisonReportId: UUID): Option[ComparisonReport] = {
    val path = root / "reports" / s"${comparisonReportId.toString}.bin"
    Try(path.readDeserialized[ComparisonReport]()).toOption
  }

  def getReportFile(path: String): Option[File] = {
    val file = root / path
    Option.when(file.isReadable)(file)
  }
}

object SubmissionRepository {
  def createFile(contestId: ContestId, submissionId: SubmissionId, languageOption: LanguageOption, root: File): File = {
    val fileExtension = languageOption match {
      case LanguageOption.C_CPP    => s"cpp"
      case LanguageOption.JAVA     => s"java"
      case LanguageOption.PYTHON_3 => s"py"
    }
    val fileName = s"${submissionId.value}.$fileExtension"
    (root / contestId.value.toString / fileName)
      .createIfNotExists(createParents = true)
  }

  def getFile(contestId: ContestId, submissionId: SubmissionId, root: File): Option[File] =
    (root / contestId.value.toString).list
      .find(_.nameWithoutExtension == submissionId.value.toString)
}
