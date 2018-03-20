(ns test.mazes.generators.sidewinder-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]
            [mazes.grid :as g]
            [mazes.generators.sidewinder :as sidewinder]))

(stest/instrument)

(deftest test-generate
  (let [grid (g/create-grid 3 3)
        maze (sidewinder/generate grid)]
    (is (spec/valid? ::g/grid maze))))
