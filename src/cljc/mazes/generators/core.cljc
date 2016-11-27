(ns mazes.generators.core
  "Top-level namespace defining properties used in maze generation algorithms."
  (:require [mazes.grid :as g]
            [clojure.spec :as spec]))

; If we whitelist permitted values, it's hard to add implementations.
(spec/def ::algorithm keyword?)

; used in multi-spec
(defmulti step-values ::algorithm)

; maze generation api
(defmulti initial-values
  "Given a grid and an ::a/algorithm keyword, returns a ::a/step-values
  representing the initial state of the generation."
  (fn [grid algorithm] algorithm))

(defmulti step
  "Given a ::a/step-values, executes one step of the binary tree algorithm and
  returns a new ::a/step-values representing the new state of the algorithm."
  ::algorithm)

(defmulti complete?
  "Given a ::a/step-values, returns true if the maze generation is complete."
  ::algorithm)

(defmulti result
  "Given a ::a/step-values, returns the final result of the maze generation; in
  this case, a grid."
  ::algorithm)

(defmulti generate
  "Given a ::g/grid and a ::a/algorithm, returns a ::g/grid which has been
  connected by the given algorithm."
  (fn [grid algorithm] algorithm))

; TODO: move this to the aldous-broder namespace
(defmethod step-values ::aldous-broder [_]
  (spec/keys :req [::algorithm ::g/grid ::g/coordinates ::unconnected]))

