(ns test.mazes.algorithms.binary-tree-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as grid]
            [mazes.algorithms.binary-tree :as btree]))

(stest/instrument)

(deftest test-generate
  (let [grid (grid/create-grid 3 3)
        maze (btree/generate grid)]
    (is (spec/valid? ::grid/grid maze))))
