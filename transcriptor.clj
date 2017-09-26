;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

(ns cognitect.transcriptor
  (:import clojure.lang.LineNumberingPushbackReader java.io.File)
  (:require
   [clojure.core.server :as server]
   [clojure.java.io :as io]
   [clojure.main :as main]
   [clojure.pprint :as pp]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(defmacro check!
  "Checks v (defaults to *1) against spec, throwing on failure. Returns nil."
  ([spec]
     `(check! ~spec *1))
  ([spec v]
     `(let [v# ~v]
        (when-not (s/valid? ~spec v#)
          (let [ed# (s/explain-data ~spec v#)
                err# (ex-info (str "Transcript assertion failed! " (with-out-str (s/explain-out ed#)))
                              ed#)]
            (throw err#))))))

(def ^:private ^:dynamic *exit-items* ::disabled)

(defn on-exit
  "If running inside a call to repl, queue f to run when REPL exits."
  [f]
  (when-not (= ::disabled *exit-items*)
    (swap! *exit-items* conj f))
  nil)

(defn repl
  "Transcript-making REPL. Like a normal REPL except:

- pretty prints inputs
- prints '=> ' before pretty printing results
- throws on exception

Not intended for interactive use -- point this at a file to
produce a transcript as-if a human had performed the
interactions."
  []
  (let [cl (.getContextClassLoader (Thread/currentThread))]
    (.setContextClassLoader (Thread/currentThread) (clojure.lang.DynamicClassLoader. cl)))
  (let [request-prompt (Object.)
        request-exit (Object.)
        read-eval-print
        (fn []
          (let [read-eval *read-eval*
                input (main/with-read-known (server/repl-read request-prompt request-exit))]
            (if (#{request-prompt request-exit} input)
              input
              (do
                (pp/pprint input)
                (let [value (binding [*read-eval* read-eval] (eval input))]
                  (set! *3 *2) (set! *2 *1) (set! *1 value)
                  (print "=> ")
                  (pp/pprint value)
                  (println))))))]
    (main/with-bindings
      (binding [*exit-items* (atom ())]
        (try
         (loop []
           (let [value (read-eval-print)]
             (when-not (identical? value request-exit)
               (recur))))
         (finally
          (doseq [item @*exit-items*]
            (item))))))))

(defn- repl-on
  [r]
  (with-open [rdr (LineNumberingPushbackReader. (io/reader r))]
    (binding [*source-path* (str r) *in* rdr]
      (repl))))

(def script-counter (atom 0))

(defn run
  "Run script through transcripting repl in a tearoff namespace."
  [script]
  (let [ns (symbol (str "cognitect.transcriptor.t_" (swap! script-counter inc)))]
    (prn (list 'comment {:transcript (str script) :namespace ns}))
    (binding [*ns* *ns*]
      (in-ns ns)
      (clojure.core/use 'clojure.core)
      (repl-on script))))

(defn repl-files
  "Returns a seq of .repl files under dir"
  [dir]
  (->> (io/file dir)
       file-seq
       (filter (fn [^java.io.File f]
                 (and (.isFile f)
                      (str/ends-with? (.getName f) ".repl"))))
       (map #(.getPath ^File %))))
