package viper.gobra.frontend.info.implementation.typing.ghost

import org.bitbucket.inkytonik.kiama.util.Messaging.{Messages, message}
import viper.gobra.ast.frontend._
import viper.gobra.frontend.info.base.SymbolTable.{Constant, Embbed, Field, Function, MethodImpl, Variable}
import viper.gobra.frontend.info.base.Type.{BooleanT, Type}
import viper.gobra.frontend.info.implementation.TypeInfoImpl
import viper.gobra.frontend.info.implementation.typing.BaseTyping
import viper.gobra.util.Violation.violation

trait GhostExprTyping extends BaseTyping { this: TypeInfoImpl =>

  private[typing] def wellDefGhostExpr(expr: PGhostExpression): Messages = expr match {
    case POld(op) => isPureExpr(op)
    case PConditional(cond, thn, els) =>
      // check that cond is of type bool:
      comparableTypes.errors(exprType(cond), BooleanT)(expr) ++
      // check that thn and els have a common type
      mergeableTypes.errors(exprType(thn), exprType(els))(expr)
  }

  private[typing] def ghostExprType(expr: PGhostExpression): Type = expr match {
    case POld(op) => exprType(op)
    case PConditional(cond, thn, els) =>
      typeMerge(exprType(thn), exprType(els)).getOrElse(violation("no common supertype found"))
  }

  private[typing] def isPureExpr(expr: PExpression): Messages = {
    message(expr, s"expected pure expression but got $expr", !isPureExprAttr(expr))
  }

  private def isPureId(id: PIdnNode): Boolean = entity(id) match {
    case _: Constant => true
    case _: Variable => true
    case _: Field => true
    case _: Embbed => true
    case Function(decl, _) => decl.spec.isPure
    case MethodImpl(decl, _) => decl.spec.isPure
    case _ => false
  }

  private lazy val isPureExprAttr: PExpression => Boolean =
    attr[PExpression, Boolean] {
      case n@ PNamedOperand(id) => isPureId(id)
      case _: PBoolLit | _: PIntLit | _: PNilLit => true

      case n@PCall(base, paras) => isPureExprAttr(base) && paras.forall(isPureExprAttr)

      case n: PConversionOrUnaryCall =>
        resolveConversionOrUnaryCall(n)
        { case (_, _) => false }
        { case (id, arg) => isPureId(id) && isPureExprAttr(arg)}
          .getOrElse(false)

      case n@PMethodExpr(t, id) => isPureId(id)
      case n@PSelection(base, id) => isPureExprAttr(base) && isPureId(id)
      case n@PSelectionOrMethodExpr(base, id) => isPureId(id)

      case n@PReference(e) => isPureExprAttr(e)
      case n@PDereference(exp) => isPureExprAttr(exp)

      case PNegation(e) => isPureExprAttr(e)

      case x: PBinaryExp => isPureExprAttr(x.left) && isPureExprAttr(x.right) && (x match {
          case _: PEquals | _: PUnequals |
               _: PAnd | _: POr |
               _: PLess | _: PAtMost | _: PGreater | _: PAtLeast |
               _: PAdd | _: PSub | _: PMul | _: PMod | _: PDiv => true
          case _ => false
        })

      case n: PUnfolding => true

      case _: POld => true

      case PConditional(cond, thn, els) => Seq(cond, thn, els).forall(isPureExprAttr)

      case n@PCompositeLit(t, lit) => true

      // Might change soon:
      case n@PIndexedExp(base, index) => false

      // Might change as some point
      case _: PFunctionLit => false
      case n@PConversion(t, arg) => false
      case n@PSliceExp(base, low, high, cap) => false

      // Others
      case n@PTypeAssertion(base, typ) => false
      case n@PReceive(e) => false
    }


}
