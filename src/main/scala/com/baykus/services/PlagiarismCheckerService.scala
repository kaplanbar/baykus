package com.baykus.services

import com.baykus.models._
import com.baykus.repositories.SubmissionRepository
import com.typesafe.config.Config
import de.jplag.JPlag
import de.jplag.options.{JPlagOptions, LanguageOption}
import de.jplag.reporting.Report

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait PlagiarismCheckerService {

  /** Add the submission to the contest according to its contestId field */
  def addCode(submission: Submission): Future[Unit]

  /** Get source code from submission id */
  def getCode(submissionId: SubmissionId, contestId: ContestId): Future[Option[SourceCode]]

  /** Generate results if not generated yet and return the HTML reports for each comparison */
  def generateComparisonReports(contestId: ContestId): Future[Seq[ComparisonReport]]

  /** Get comparison report for a comparison report id. */
  def getComparisonReport(comparisonReportId: UUID): Future[Option[ComparisonReport]]

}

class PlagiarismCheckerServiceImpl(config: Config, submissionRepository: SubmissionRepository)(implicit ec: ExecutionContext)
    extends PlagiarismCheckerService {

  override def addCode(submission: Submission): Future[Unit] =
    submissionRepository.saveSubmission(submission)

  override def getCode(submissionId: SubmissionId, contestId: ContestId): Future[Option[SourceCode]] =
    submissionRepository.getSourceCode(contestId, submissionId)

  private val languageOptions = Seq(LanguageOption.JAVA)

  override def generateComparisonReports(contestId: ContestId): Future[Seq[ComparisonReport]] = {
    submissionRepository.clearReports(contestId)
    Future
      .traverse(languageOptions) { languageOption =>
        Future {
          val options = new JPlagOptions(submissionRepository.sourceCodePath(contestId).toString, languageOption)
          val result  = new JPlag(options).run()
          val report  = new Report(submissionRepository.reportPath(contestId).toFile, options)
          report.writeResult(result)
        }
      }
      .flatMap { _ =>
        submissionRepository.comparisonReports(contestId)
      }
  }

  override def getComparisonReport(comparisonReportId: UUID): Future[Option[ComparisonReport]] =
    Future(submissionRepository.getComparisonReport(comparisonReportId))

}
