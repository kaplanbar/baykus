package com.baykus.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.baykus.models.Submission.JsonProtocol._
import com.baykus.models.{ContestId, Submission, SubmissionId}
import com.baykus.services.PlagiarismCheckerService

import java.util.UUID
import scala.util.{Failure, Success}

class Routers(plagiarismCheckerService: PlagiarismCheckerService) extends SprayJsonSupport {
  private val submissionRoute: Route =
    path("submission") {
      post {
        entity(as[Submission]) { submission =>
          onComplete(plagiarismCheckerService.addCode(submission)) {
            case Failure(exception) => complete(exception)
            case Success(_)         => complete(s"Submission ${submission.submissionId.value} successfuly added")
          }
        }
      } ~
        get {
          parameter("submission_id".as[UUID], "contest_id".as[UUID]) { (submissionId, contestId) =>
            onComplete(plagiarismCheckerService.getCode(SubmissionId(submissionId), ContestId(contestId))) {
              case Failure(exception)        => complete(exception)
              case Success(Some(sourceCode)) => complete(sourceCode)
              case Success(None)             => complete("Submission not found")
            }
          }
        }
      // TODO: Add delete, get by submission id
    }
  private val comparisonReportRoute: Route =
    pathPrefix("report") {
      path("generate") {
        get {
          // TODO: Add unmarshaller for ContestId
          parameter("contest_id".as[UUID]) { contestId =>
            onComplete(plagiarismCheckerService.generateComparisonReports(ContestId(contestId))) {
              case Failure(exception) => complete(exception)
              case Success(reports)   => complete(reports.map(_.comparisonReportId))
            }
          }
        }
      } ~
        path("view") {
          get {
            parameter("report_id".as[UUID]) { comparisonReportId =>
              onComplete(plagiarismCheckerService.getComparisonReport(comparisonReportId)) {
                case Failure(exception) => complete(exception)
                case Success(Some(result)) =>
                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, html.comparison(result).body))
                case Success(None) => complete("Report file not found")
              }
            }
          }
        }
    }

  private val imagesRoute: Route = pathPrefix("images") {
    getFromResourceDirectory("images")
  }
  val route: Route =
    pathPrefix("api") {
      concat(
        submissionRoute,
        comparisonReportRoute,
        imagesRoute
      )
    }
}
