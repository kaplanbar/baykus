package com.baykus
import com.baykus.models.{ContestId, SourceCode, Submission, SubmissionId, User}
import de.jplag.options.LanguageOption
import org.scalacheck.Gen

trait Generators {
  val genContestId: Gen[ContestId]       = Gen.uuid.map(ContestId.apply)
  val genSourceCode: Gen[SourceCode]     = Gen.asciiPrintableStr.suchThat(_.nonEmpty).map(SourceCode.apply)
  val genSubmissionId: Gen[SubmissionId] = Gen.uuid.map(SubmissionId.apply)
  val genUser: Gen[User]                 = Gen.asciiPrintableStr.map(User.apply)
  val genLanguageOption: Gen[LanguageOption] =
    Gen.oneOf(LanguageOption.JAVA, LanguageOption.C_CPP, LanguageOption.PYTHON_3)
  val genSubmission: Gen[Submission] = for {
    contestId      <- genContestId
    sourceCode     <- genSourceCode
    submissionId   <- genSubmissionId
    user           <- genUser
    languageOption <- genLanguageOption
  } yield Submission(contestId, sourceCode, user, submissionId, languageOption)

}
