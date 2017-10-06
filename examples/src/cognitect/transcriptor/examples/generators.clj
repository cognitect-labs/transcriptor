(ns cognitect.transcriptor.examples.generators
  (:require [clojure.spec.alpha :as s]))

(defn one-of
  [s]
  (-> (s/exercise s) last first))
