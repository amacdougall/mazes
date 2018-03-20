(ns test.mazes.generators.binary-tree-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]
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
        actual (b/initial-values grid)]
    (is (spec/valid? ::a/step-values actual))
    (is (= expected actual))))

(deftest test-step
  (let [grid (g/create-grid 2 2)
        initial-values (b/initial-values grid)
        initial-cell (::g/cell initial-values)
        updated-values (b/step initial-values)]
    (is (not= initial-values updated-values))
    (is (spec/valid? ::a/step-values updated-values))
    (is (= (g/next-cell grid initial-cell)
           (::g/cell updated-values)))
    ; cell 0 0 should now have an exit to the east or south
    (is (some #{::g/e ::g/s}
              (-> updated-values ::g/grid (g/find-cell [0 0]) ::g/exits)))))

(deftest test-complete?
  ; complete? is specced to require a true ::a/step-values
  (let [step-values (b/initial-values (g/create-grid 2 2))]
    (is (not (b/complete? step-values)))
    (is (b/complete? (assoc step-values ::g/cell nil)))))

(deftest test-result
  (let [step-values (b/initial-values (g/create-grid 2 2))]
    (is (= (::g/grid step-values) (b/result step-values)))))

(deftest test-generate
  (let [grid (g/create-grid 5 5)
        maze (b/generate grid)]
    (is (spec/valid? ::g/grid maze))
    (let [origin (g/find-cell maze [0 0])
          destination (g/find-cell maze [(dec (g/row-count grid))
                                         (dec (g/column-count grid))])
          path (::d/path (d/solve maze origin destination))]
      (is (not (nil? path))
          "maze must have a path from the origin to the destination"))))
