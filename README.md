# transcriptor

Convert REPL interactions into example-based tests.

# Using

transcriptor is in the Maven Central repository.

Clojure deps.edn:

    com.cognitect/transcriptor {:mvn/version "0.1.5"}

lein project.clj:

    [com.cognitect/transcriptor "0.1.5"]

# Problem

Testing frameworks often introduce their own abstractions for
e.g. evaluation order, data validation, reporting, scope, code reuse,
state, and lifecycle. In my experience, these abstractions are
*always* needlessly different from (and inferior to) related
abstractions provided by the language itself.

Adapting an already-working REPL interaction to satisfy such testing
abstractions is a waste of time, and it throws away the intermediate
REPL results that are valuable in diagnosing a problem.

So transcriptor aims to do *less*, and impose the bare minimum of
cognitive load needed to convert a REPL interaction into a test. The
entire API is four functions:

* `xr/run` runs a REPL script and produces a transcript
* `check!` validates the last returned value against a Clojure spec
* `xr/on-exit` lets you register cleanup code to run after `xr/run` completes
* `xr/repl-files` finds the `.repl` files in a directory tree

# Approach

Work at the REPL. Whenver you want to convert a chunk of work into a
test, just copy it into a file with a .repl suffix. You can later call
`xr/run` on a REPL file:

    (require '[cognitect.transcriptor :as xr :refer (check!)])
    (xr/run "your-file-name-here.repl")

`run` launches a REPL that consumes all forms in the file passed
in. `run` will

* isolate execution in a one-off namespace whose name is printed to
  stdout. (If the script fails, you can enter this namespace and poke around.)
* pretty print every evaluation result, providing a transcript as
  if you had repeated the REPL interactions by hand.

# Evaluation Order

Clojure language (REPL) semantics.

# Validation

Clojure language semantics plus one function.

transcriptor includes a single validation form, `check!`, that will
check an argument (by default `*1`) against a provided spec, throwing
an exception if the error does not match:

    (+ 1 1)
    (check! even?)

Exceptions are failures and unwind the stack back to the call to `xr/run`.

# Reporting

Read clojure.spec error data directly, or pipe it to an error reporter
or visualizer of your choice.

# Code reuse

Clojure language semantics. Write functions in namespaces and have
.repl scripts require them as needed.

# Scope

Clojure language semantics. `def` vars that you need.

# State

Clojure language semantics. (For testing code with nontrivial state I
recommend simulation-based testing instead).

# Lifecycle

Clojure language semantics plus one function.

The `xr/on-exit` function is a no-op outside `xr/run`. Inside, it will
  queue a function that will be called after the REPL exits.

# Test Automation

Clojure language semantics plus one function.

* `xr/repl-files` returns a seq of .repl files under a directory root, suitable
  for passing to `xr/run`.

# Test Repeatability

Clojure language semantics.

# Keep Dumb Tests Ugly

Tests that want an exact value match can use a Clojure set as a spec:

    (+ 1 2)
    (check! #{3})  ;; duh

This is ugly by design, as an inducement to test properties instead of
specifics.

# License

Eclipse Public License, same as Clojure.
https://www.eclipse.org/legal/epl-v10.html

# Contributing

Please open a Github issue if your have feedback.
