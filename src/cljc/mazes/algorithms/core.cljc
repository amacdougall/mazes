(ns mazes.algorithms.core
  "Top-level namespace defining properties used in maze generation algorithms."
  (:require [mazes.grid :as g]
            [clojure.spec :as spec]))

(defmulti step-values ::algorithm)

(defmethod step-values ::aldous-broder [_]
  (spec/keys :req [::algorithm ::g/grid ::g/coordinates ::unconnected]))



