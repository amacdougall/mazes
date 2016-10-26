(ns test.mazes.algorithms.sidewinder-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as g]
            [mazes.algorithms.sidewinder :as sidewinder]))

(stest/instrument)

(deftest test-generate
  (let [grid (g/create-grid 3 3)
        maze (sidewinder/generate grid)]
    (is (spec/valid? ::g/grid maze))))
