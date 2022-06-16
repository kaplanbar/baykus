package com.baykus

import com.baykus.repositories.SubmissionRepository
import com.baykus.services.{PlagiarismCheckerService, PlagiarismCheckerServiceImpl}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{BeforeAndAfterAll, Suite}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

// TODO: Refactor tests to be able to use should syntax.
class PlagiarismCheckerServiceImplSpec extends Properties("PlagiarismCheckerServiceImpl") with Generators with Suite with BeforeAndAfterAll {
  val config: Config                                     = ConfigFactory.load()
  implicit val ec: ExecutionContext                      = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val submissionRepository: SubmissionRepository         = new SubmissionRepository(config)
  val plagiarismCheckerService: PlagiarismCheckerService = new PlagiarismCheckerServiceImpl(config, submissionRepository)

  property("add code and get source code back") = {
    forAll(genSubmission) { submission =>
      plagiarismCheckerService.addCode(submission).futureValue
      plagiarismCheckerService
        .getCode(submission.submissionId, submission.contestId)
        .futureValue
        .value == submission.sourceCode
    }
  }
}
