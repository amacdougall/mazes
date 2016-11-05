(ns test.mazes.algorithms.wilson-test
  (:require [clojure.test :refer :all]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as g]
            [mazes.algorithms.wilson :as w]
            [mazes.algorithms.dijkstra :as d]))

(stest/instrument)

;; NOTE: Since Wilson's algorithm is a random walk, it has indeterminate
;; runtime, increasing rapidly with grid size.
;;
;; Even if the algorithm is not implemented correctly, it may occasionally
;; stumble into a correct layout. Increasing the grid size makes this situation
;; vanishingly unlikely, but increasing it too far also makes the algorithm run
;; very slowly. 5x5 seemed like an acceptable compromise.
;;
;; The :slow metadata ties in with :test-selectors in project.clj. Add the
;; :slow param to lein test to run :slow tests specifically, or :all to run all
;; tests.
(deftest ^:slow test-generate
  (let [grid (g/create-grid 5 5)
        maze (w/generate grid)]
    (is (spec/valid? ::g/grid maze))
    (let [origin (g/find-cell maze [0 0])
          destination (g/find-cell maze [(dec (g/row-count maze))
                                         (dec (g/column-count maze))])
          solution (d/solve maze origin destination)]
      (is (not (nil? (::d/path solution)))
          "maze must have a path from the origin to the destination"))))

(deftest test-rewind-to
  (testing "vector"
    (is (= [1] (w/rewind-to #{1} [1 2 3 4 5]))
        "should rewind to value-set predicate")
    (is (empty? (w/rewind-to #{10} [1 2 3 4 5]))
        "should return empty when predicate is never matched")
    (is (= [1 2 1 2] (w/rewind-to #{2} [1 2 1 2 3 4]))
        "should rewind only to the most recent predicate match")
    (is (= [1 2] (w/rewind-to even? [1 2 1 1 1]))
        "should rewind to first function predicate match"))
  (testing "list"
    ; note that all the examples are reversed due to list peek/pop semantics
    (is (= '(1) (w/rewind-to #{1} '(5 4 3 2 1)))
        "should rewind to value-set predicate")
    (is (empty? (w/rewind-to #{10} '(5 4 3 2 1)))
        "should return empty when predicate is never matched")
    (is (= '(2 1 2 1) (w/rewind-to #{2} '(4 3 2 1 2 1)))
        "should rewind only to the most recent predicate match")
    (is (= '(2 1) (w/rewind-to even? '(1 1 1 2 1)))
        "should rewind to first function predicate match")))

; Note that we can omit step values not used by the functions, although of
; course this is white-box testing at its whitest.

(deftest test-add-to-path-step
  (let [next [0 3]
        path [[0 0] [0 1] [0 2]]
        step-values {::w/next next, ::w/path path}]
    (is (= (conj path next) (::w/path (w/add-to-path-step step-values))))))

(deftest test-erase-loop-step
  (let [next [0 0]
        path [[0 0] [0 1] [1 1] [1 0]]
        step-values {::w/next next, ::w/path path}]
    (is (= [[0 0]] (::w/path (w/erase-loop-step step-values)))
        "if next coordinates cause a loop, the loop should be erased from the path")
    (is (= path (::w/path (w/erase-loop-step (assoc step-values ::w/next [2 0]))))
        "if next coordinates do not cause a loop, the path should be unchanged")))

; connect-path is the implementation behind connect-path-step; it just takes
; simplified params instead of step-values. I may refactor them back into one.
(deftest test-connect-path
  (let [grid (g/create-grid 3 3)
        next [2 0]
        path [[0 0] [1 0]]
        updated-grid (w/connect-path grid (conj path next))]
    (is (g/has-exit? (g/find-cell updated-grid [0 0]) ::g/e)
        "cell [0 0] should have an exit to the east")
    (is (g/has-exit? (g/find-cell updated-grid [1 0]) ::g/e)
        "cell [0 1] should have an exit to the east")
    (is (not (g/has-exit? (g/find-cell updated-grid [2 0]) ::g/e))
        "cell [0 2] should not have an exit to the east")))

(deftest test-connect-path-step
  (let [grid (g/create-grid 3 3)
        next [2 0]
        path [[0 0] [1 0]]
        all-coordinates (set (map g/coordinates (g/all-cells grid)))
        step-values {::g/grid grid
                     ::w/next [2 0]
                     ::w/path path
                     ::w/unconnected (disj all-coordinates [2 0])}
        updated-values (w/connect-path-step step-values)]
    (is (= (w/connect-path grid (conj path next)) (::g/grid updated-values))
        "should update grid using connect-path")
    (is (not (some (partial contains? (::w/unconnected updated-values)) path))
        "should remove path from unconnected set")
    (is (empty? (::w/path updated-values))
        "should clear the path")))

(deftest test-step-function
  (let [grid (g/create-grid 3 3)
        all-coordinates (set (map g/coordinates (g/all-cells grid)))
        unconnected (disj all-coordinates [0 0])]
    (testing "when current and next are connected"
      (let [step-values {::g/grid grid
                         ::w/current [0 1]
                         ::w/next [0 0]
                         ::w/unconnected (disj unconnected [0 1])
                         ::w/path []}] ; path is irrelevant
        (is (= identity (w/step-function step-values)))))
    (testing "when next is connected, but current is not"
      (let [step-values {::g/grid grid
                         ::w/current [0 1]
                         ::w/next [0 0]
                         ::w/unconnected unconnected
                         ::w/path [[0 2] [0 1]]}]
        (is (= w/connect-path-step (w/step-function step-values)))))
    (testing "when current and next are unconnected, and path has a loop"
      (let [step-values {::g/grid grid
                         ::w/current [1 2]
                         ::w/next [2 2]
                         ::w/unconnected unconnected
                         ::w/path [[2 2] [2 1] [1 1] [1 2]]}]
        (is (= w/erase-loop-step (w/step-function step-values)))))
    (testing "when current and next are unconnected, and path has no loop"
      (let [step-values {::g/grid grid
                         ::w/current [2 1]
                         ::w/next [2 0]
                         ::w/unconnected unconnected
                         ::w/path [[2 2] [2 1]]}]
        (is (= w/add-to-path-step (w/step-function step-values)))))))
