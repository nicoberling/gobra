// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2011-2020 ETH Zurich.

package viper.gobra.translator.interfaces

import viper.gobra.ast.internal.{BuiltInFPredicate, BuiltInFunction, BuiltInMPredicate, BuiltInMethod, FPredicate, FunctionMember, LookupTable, MPredicate, MethodMember}
import viper.gobra.translator.interfaces.components._
import viper.gobra.translator.interfaces.translator._
import viper.silver.{ast => vpr}
import viper.gobra.ast.{internal => in}
import viper.gobra.translator.encodings.TypeEncoding

trait Context {

  // components
  def field: Fields
  def array : Arrays
  def seqToSet : SeqToSet
  def seqToMultiset : SeqToMultiset
  def seqMultiplicity : SeqMultiplicity
  def option : Options
  def optionToSeq : OptionToSeq
  def slice : Slices
  def fixpoint: Fixpoint
  def tuple: Tuples
  def equality: Equality
  def condition: Conditions
  def unknownValue: UnknownValues

  // translator
  def typeEncoding: TypeEncoding
  def ass: Assertions
  def expr: Expressions
  def method: Methods
  def pureMethod: PureMethods
  def predicate: Predicates
  def builtInMembers: BuiltInMembers
  def stmt: Statements
  def measures: TerminationMeasures

  // lookup
  def table: LookupTable
  def lookup(t: in.DefinedT): in.Type = table.lookup(t)
  def lookup(f: in.FunctionProxy): in.FunctionMember = table.lookup(f) match {
    case fm: FunctionMember => fm
    case bf: BuiltInFunction => builtInMembers.function(bf)(this)
  }
  def lookup(m: in.MethodProxy): in.MethodMember = table.lookup(m) match {
    case mm: MethodMember => mm
    case bm: BuiltInMethod => builtInMembers.method(bm)(this)
  }
  def lookup(p: in.MPredicateProxy): in.MPredicate = table.lookup(p) match {
    case mp: MPredicate => mp
    case bmp: BuiltInMPredicate => builtInMembers.mpredicate(bmp)(this)
  }
  def lookup(p: in.FPredicateProxy): in.FPredicate = table.lookup(p) match {
    case fp: FPredicate => fp
    case bfp: BuiltInFPredicate => builtInMembers.fpredicate(bfp)(this)
  }

  def underlyingType(t: in.Type): in.Type = t match {
    case t: in.DefinedT => underlyingType(lookup(t))
    case t => t
  }

  // mapping

  def addVars(vars: vpr.LocalVarDecl*): Context

  // fresh variable counter
  /** publicly exposed infinite iterator providing fresh names */
  def freshNames: Iterator[String] = internalFreshNames
  /** internal fresh name iterator that additionally provides a getter function for its counter value */
  protected def internalFreshNames: FreshNameIterator

  /** copy constructor */
  def :=(
          fieldN: Fields = field,
          arrayN : Arrays = array,
          seqToSetN : SeqToSet = seqToSet,
          seqToMultisetN : SeqToMultiset = seqToMultiset,
          seqMultiplicityN : SeqMultiplicity = seqMultiplicity,
          optionN : Options = option,
          optionToSeqN : OptionToSeq = optionToSeq,
          sliceN : Slices = slice,
          fixpointN: Fixpoint = fixpoint,
          tupleN: Tuples = tuple,
          equalityN: Equality = equality,
          conditionN: Conditions = condition,
          unknownValueN: UnknownValues = unknownValue,
          typeEncodingN: TypeEncoding = typeEncoding,
          assN: Assertions = ass,
          measuresN: TerminationMeasures = measures,
          exprN: Expressions = expr,
          methodN: Methods = method,
          pureMethodN: PureMethods = pureMethod,
          predicateN: Predicates = predicate,
          builtInMembersN: BuiltInMembers = builtInMembers,
          stmtN: Statements = stmt,
          initialFreshCounterValueN: Int = internalFreshNames.getValue
         ): Context


  def finalize(col : Collector): Unit = {
    // components
    col.finalize(field)
    col.finalize(array)
    col.finalize(seqToSet)
    col.finalize(seqToMultiset)
    col.finalize(seqMultiplicity)
    col.finalize(option)
    col.finalize(optionToSeq)
    col.finalize(slice)
    col.finalize(fixpoint)
    col.finalize(tuple)
    col.finalize(equality)
    col.finalize(condition)
    col.finalize(unknownValue)

    // translators
    col.finalize(typeEncoding)
    col.finalize(ass)
    col.finalize(measures)
    col.finalize(expr)
    col.finalize(method)
    col.finalize(pureMethod)
    col.finalize(predicate)
    col.finalize(builtInMembers)
    col.finalize(stmt)
  }

  trait FreshNameIterator extends Iterator[String] {
    def getValue: Int
  }
}
