(ns test.mazes.generators.binary-tree-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as g]
            [mazes.generators.core :as a]
            [mazes.generators.binary-tree :as b]
            [mazes.pathfinders.dijkstra :as d]))

(stest/instrument)

(deftest test-spec
  (let [grid (g/create-grid 2 2)
        step-values {::a/algorithm ::b/binary-tree
                     ::g/grid grid
                     ::g/cell (g/find-cell grid [0 0])}]
    (is (spec/valid? ::a/step-values step-values))))

(deftest test-initial-values
  (let [grid (g/create-grid 2 2)
        expected {::a/algorithm ::b/binary-tree
                  ::g/grid grid
                  ::g/cell (g/find-cell grid [0 0])}
        actual (a/initial-values grid ::b/binary-tree)]
    (is (spec/valid? ::a/step-values actual))
    (is (= expected actual))))

(deftest test-step
  (let [grid (g/create-grid 2 2)
        initial-values (a/initial-values grid ::b/binary-tree)
        initial-cell (::g/cell initial-values)
        updated-values (a/step initial-values)]
    (is (not= initial-values updated-values))
    (is (spec/valid? ::a/step-values updated-values))
    (is (= (g/next-cell grid initial-cell)
           (::g/cell updated-values)))
    ; cell 0 0 should now have an exit to the east or south
    (is (some #{::g/e ::g/s}
              (-> updated-values ::g/grid (g/find-cell [0 0]) ::g/exits)))))

(deftest test-complete?
  ; complete? is specced to require a true ::a/step-values
  (let [step-values (a/initial-values (g/create-grid 2 2) ::b/binary-tree)]
    (is (not (a/complete? step-values)))
    (is (a/complete? (assoc step-values ::g/cell nil)))))

(deftest test-result
  (let [step-values (a/initial-values (g/create-grid 2 2) ::b/binary-tree)]
    (is (= (::g/grid step-values) (a/result step-values)))))

(deftest test-generate
  (let [grid (g/create-grid 5 5)
        maze (a/generate grid ::b/binary-tree)]
    (is (spec/valid? ::g/grid maze))
    (let [origin (g/find-cell maze [0 0])
          destination (g/find-cell maze [(dec (g/row-count grid))
                                         (dec (g/column-count grid))])
          path (::d/path (d/solve maze origin destination))]
      (is (not (nil? path))
          "maze must have a path from the origin to the destination"))))
