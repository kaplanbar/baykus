package com.baykus.models

import com.baykus.models.ComparisonReport.UserInfo
import de.jplag.options.LanguageOption

import java.util.UUID

case class ComparisonReport(comparisonReportId: UUID, user1Info: UserInfo, user2Info: UserInfo, similarity: Double, language: LanguageOption)

object ComparisonReport {
  case class UserInfo(user: User, highlightedSourceCode: String)
}
