package com.baykus

import akka.actor.ActorSystem
import com.baykus.api.Routers
import com.baykus.repositories.SubmissionRepository
import com.baykus.services.{PlagiarismCheckerService, PlagiarismCheckerServiceImpl}
import com.typesafe.config.{Config, ConfigFactory}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait ApplicationModule {
  // TODO: Check how to create a proper execution context
  implicit val system: ActorSystem  = ActorSystem("Baykus")
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  val config: Config                                     = ConfigFactory.load()
  val submissionRepository: SubmissionRepository         = new SubmissionRepository(config)
  val plagiarismCheckerService: PlagiarismCheckerService = new PlagiarismCheckerServiceImpl(config, submissionRepository)
  val routers: Routers                                   = new Routers(plagiarismCheckerService)
  val application: Baykus                                = new Baykus(plagiarismCheckerService, routers)
}
