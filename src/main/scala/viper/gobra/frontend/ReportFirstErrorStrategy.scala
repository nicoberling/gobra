package viper.gobra.frontend

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.{BailErrorStrategy, DefaultErrorStrategy, FailedPredicateException, InputMismatchException, Parser, ParserRuleContext, RecognitionException, Token}
import viper.gobra.frontend.GobraParser.{BlockContext, ExprSwitchStmtContext}

class ReportFirstErrorStrategy extends DefaultErrorStrategy {


  override def sync(recognizer: Parser): Unit = {  }

  override def reportFailedPredicate(recognizer: Parser, e: FailedPredicateException): Unit = {
    val msg = e.getMessage
    recognizer.notifyErrorListeners(e.getOffendingToken, msg, e)
  }

  override def recover(recognizer: Parser, e: RecognitionException): Unit = {
    var context = recognizer.getContext
    // For blocks that could be interpreted as struct literals (like `if a == b { }` or `switch tag := 0; tag { }`)
    // Cast a wide net to catch every case, but still allow faster parsing if the error can't be an ambiguity.
    context match {
      case _ : BlockContext => throw new AmbiguityException
      case _ : ExprSwitchStmtContext => throw new AmbiguityException
      case _ =>
    }
    while (context != null) {
      context.exception = e
      context = context.getParent
    }
    throw new ParseCancellationException(e)
  }

  override def recoverInline(recognizer: Parser): Token = {
    val e = new InputMismatchException(recognizer)
    var context = recognizer.getContext
    context match {
      case _ : BlockContext => throw new AmbiguityException
      case _ : ExprSwitchStmtContext => throw new AmbiguityException
      case _ =>
    }
    while ( context != null) {
      context.exception = e
      context = context.getParent
    }
    reportError(recognizer, e)
    throw new ParseCancellationException(e)
  }

  def forceReport(recognizer: Parser, e: RecognitionException): Unit = {
    val tmp = errorRecoveryMode
    errorRecoveryMode = false
    reportError(recognizer, e)
    errorRecoveryMode = tmp
  }


}

class AmbiguityException() extends RuntimeException