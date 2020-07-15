package main

// ##(-I src/test/resources/regressions/features/import/constant_import)
import b "bar2"

// test whether constant evaluation works across packages
const Answer = b.Answer
const HasAnswer = b.HasAnswer

func foo() {
  assert(b.Answer == 42)
  assert(Answer == 42)
  assert(b.HasAnswer)
  assert(HasAnswer)
}
