package com.baykus

import com.baykus.repositories.SubmissionRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class SubmissionRepositorySpec extends Properties("SubmissionRepository") with Generators {
  val config: Config                             = ConfigFactory.load()
  implicit val ec: ExecutionContext              = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  val submissionRepository: SubmissionRepository = new SubmissionRepository(config)

  property("save a submission and get source code back") = {
    forAll(genSubmission) { submission =>
      (for {
        _          <- submissionRepository.saveSubmission(submission)
        sourceCode <- submissionRepository.getSourceCode(submission.contestId, submission.submissionId)
      } yield sourceCode.contains(submission.sourceCode)).futureValue
    }
  }

}
