package com.baykus.models

import java.nio.file.{Path, Paths}
import java.util.UUID

final case class ContestId(value: UUID) extends AnyVal

object ContestId {
  def directoryPath(rootDir: Path, contestId: ContestId): Path =
    Paths.get(rootDir.toString, s"${contestId.value}")
}
