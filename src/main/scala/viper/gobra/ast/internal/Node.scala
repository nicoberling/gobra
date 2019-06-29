/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package viper.gobra.ast.internal

import viper.gobra.ast.internal.utility.{GobraStrategy, Nodes}
import viper.gobra.reporting.Source
import viper.silver.ast.utility.Rewriter.{StrategyBuilder, Traverse}
import viper.silver.ast.utility.Rewriter.Traverse.Traverse
import viper.silver.ast.utility.Visitor
import viper.silver.{ast => vpr}

import scala.reflect.ClassTag

trait Node extends Rewritable with Product {

  def info: Source.Parser.Info

  def getMeta: Node.Meta = info

  def undefinedMeta: Boolean = getMeta == Source.Parser.Unsourced

  def pretty(prettyPrinter: PrettyPrinter = Node.defaultPrettyPrinter): String = prettyPrinter.format(this)

  lazy val formatted: String = pretty()

  override def toString: String = formatted

  /**
    * Duplicate children. Children list must be in the same order as in getChildren
    *
    * @see [[Rewritable.getChildren()]] to see why we use type AnyRef for children
    * @param children New children for this node
    * @return Duplicated node
    */
  override def duplicate(children: scala.Seq[AnyRef]): this.type =
    GobraStrategy.gobraDuplicator(this, children, getMeta)

  def duplicateMeta(newMeta: Node.Meta): this.type =
    GobraStrategy.gobraDuplicator(this, getChildren, newMeta)

  // Visitor and Nodes utilities

  /** @see [[Nodes.subnodes()]] */
  def subnodes: Seq[Node] = Nodes.subnodes(this)

  /** @see [[Visitor.reduceTree()]] */
  def reduceTree[A](f: (Node, Seq[A]) => A): A = Visitor.reduceTree(this, Nodes.subnodes)(f)

  /** @see [[Visitor.reduceWithContext()]] */
  def reduceWithContext[C, R](context: C, enter: (Node, C) => C, combine: (Node, C, Seq[R]) => R): R = {
    Visitor.reduceWithContext(this, Nodes.subnodes)(context, enter, combine)
  }

  /** Applies the function `f` to the AST node, then visits all subnodes. */
  def foreach[A](f: Node => A): Unit = Visitor.visit(this, Nodes.subnodes) { case a: Node => f(a) }

  /** @see [[Visitor.visit()]] */
  def visit[A](f: PartialFunction[Node, A]) {
    Visitor.visit(this, Nodes.subnodes)(f)
  }

  /** @see [[Visitor.visitWithContext()]] */
  def visitWithContext[C](c: C)(f: C => PartialFunction[Node, C]) {
    Visitor.visitWithContext(this, Nodes.subnodes, c)(f)
  }

  /** @see [[Visitor.visitWithContextManually()]] */
  def visitWithContextManually[C, A](c: C)(f: C => PartialFunction[Node, A]) {
    Visitor.visitWithContextManually(this, Nodes.subnodes, c)(f)
  }

  /** @see [[Visitor.visit()]] */
  def visit[A](f1: PartialFunction[Node, A], f2: PartialFunction[Node, A]) {
    Visitor.visit(this, Nodes.subnodes, f1, f2)
  }

  /** @see [[Visitor.visitOpt()]] */
  def visitOpt(f: Node => Boolean) {
    Visitor.visitOpt(this, Nodes.subnodes)(f)
  }

  /** @see [[Visitor.visitOpt()]] */
  def visitOpt[A](f1: Node => Boolean, f2: Node => A) {
    Visitor.visitOpt(this, Nodes.subnodes, f1, f2)
  }

  /** @see [[Visitor.existsDefined()]] */
  def existsDefined[A](f: PartialFunction[Node, A]): Boolean = Visitor.existsDefined(this, Nodes.subnodes)(f)

  /** @see [[Visitor.hasSubnode()]] */
  def hasSubnode(toFind: Node): Boolean = Visitor.hasSubnode(this, toFind, Nodes.subnodes)

  /** @see [[viper.silver.ast.utility.ViperStrategy]] */
  def transform(pre: PartialFunction[Node, Node] = PartialFunction.empty,
                recurse: Traverse = Traverse.Innermost)
  : this.type =
    StrategyBuilder.Slim[Node](pre, recurse) execute[this.type] this

  def replace(original: Node, replacement: Node): this.type =
    this.transform { case `original` => replacement }

  def replace[N <: Node : ClassTag](replacements: Map[N, Node]): this.type =
    if (replacements.isEmpty) this
    else this.transform { case t: N if replacements.contains(t) => replacements(t) }

  /** @see [[Visitor.deepCollect()]] */
  def deepCollect[A](f: PartialFunction[Node, A]): Seq[A] =
    Visitor.deepCollect(Seq(this), Nodes.subnodes)(f)

  /** @see [[Visitor.shallowCollect()]] */
  def shallowCollect[R](f: PartialFunction[Node, R]): Seq[R] =
    Visitor.shallowCollect(Seq(this), Nodes.subnodes)(f)

  def contains(n: Node): Boolean = this.existsDefined {
    case `n` =>
  }

  def contains[N <: Node : ClassTag]: Boolean = {
    val clazz = implicitly[ClassTag[N]].runtimeClass
    this.existsDefined {
      case n: N if clazz.isInstance(n) =>
    }
  }

}

object Node {

  val defaultPrettyPrinter = new DefaultPrettyPrinter()

  type Meta = Source.Parser.Info

  implicit class RichNode[N <: Node](n: N) {

    def deepclone: N = new utility.Rewriter().deepclone(n)

    def withInfo(newInfo: Source.Parser.Info): N = GobraStrategy.gobraDuplicator(n, n.getChildren, newInfo)

    def withMeta(newMeta: Meta): N = withInfo(newMeta)

    def verifierInfo: vpr.Info = n.info.toInfo(n)

  }
}

