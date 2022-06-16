package com.baykus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.baykus.api.Routers
import com.baykus.services.PlagiarismCheckerService

class Baykus(plagiarismCheckerService: PlagiarismCheckerService, routers: Routers)(implicit val system: ActorSystem) {
  def start() = {
    Http().newServerAt("localhost", 8080).bind(routers.route)
  }
}
