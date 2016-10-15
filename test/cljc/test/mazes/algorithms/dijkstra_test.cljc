(ns test.mazes.algorithms.dijkstra-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer [intersection]]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as grid]
            [mazes.algorithms.dijkstra :as dijkstra]))

(stest/instrument)

(def get-initial-values #'mazes.algorithms.dijkstra/get-initial-values)
(def infinite-distance #?(:clj Integer/MAX_VALUE, :cljs js/Infinity))

(deftest test-step
  (testing "simple maze"
    (let [grid (grid/create-grid 2 2)
          path [::grid/e ::grid/s ::grid/w]
          grid (grid/link-path grid (grid/find-cell grid 0 0) path)
          grid (grid/link grid (grid/find-cell grid 1 1) ::grid/w)
          origin (grid/find-cell grid 0 0)
          expected-cells (grid/cells-on-path grid origin path)
          initial-values (get-initial-values grid origin)]
      (testing "first step"
        (let [result (dijkstra/step initial-values)]
          (is (= 1 (- (count (::dijkstra/unvisited initial-values))
                      (count (::dijkstra/unvisited result))))
              "unvisited set should shrink by one")
          (is (not (contains? (::dijkstra/unvisited result) origin))
              "origin cell should be visited")
          (is (= (::dijkstra/current result) (grid/find-cell grid 1 0))
              "northeast cell should be the new current")))
      (testing "second step"
        (let [result (nth (iterate dijkstra/step initial-values) 2)]
          (is (= 2 (- (count (::dijkstra/unvisited initial-values))
                      (count (::dijkstra/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (not (contains? (::dijkstra/unvisited result) origin))
              "origin cell should be visited")
          (is (not (contains? (::dijkstra/unvisited result)
                              (grid/find-cell grid 1 0)))
              "cell east of origin should be visited")
          (is (= (::dijkstra/current result) (grid/find-cell grid 1 1))
              "southeast cell should be current")
          (is (= 2 (get (::dijkstra/distances result)
                        (::dijkstra/current result)))
              "distance to new current cell should be accurate")))))
  (testing "complex maze"
    (let [grid (grid/create-grid 3 3)
          path [::grid/e ::grid/s ::grid/e ::grid/s ::grid/e]
          grid (grid/link-path grid (grid/find-cell grid 0 0) path)
          ; give the center cell exits in each other direction
          grid (grid/link grid (grid/find-cell grid 1 1) ::grid/w)
          grid (grid/link grid (grid/find-cell grid 1 1) ::grid/s)
          origin (grid/find-cell grid 0 0)
          expected-cells (grid/cells-on-path grid origin path)
          initial-values (get-initial-values grid origin)]
      (testing "at a branching intersection"
        (let [result (nth (iterate dijkstra/step initial-values) 2)]
          (is (= 2 (- (count (::dijkstra/unvisited initial-values))
                      (count (::dijkstra/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (empty? (filter (::dijkstra/unvisited result)
                              (take 2 expected-cells)))
              "first two cells along the solution path should be visited")
          (is (= (::dijkstra/current result) (grid/find-cell grid 1 1))
              "center cell should be current")))
      (testing "after the branching intersection"
        ; As implemented, the algorithm will choose the member of the unvisited
        ; set with the lowest distance. But since it is choosing from a set, we
        ; have no knowledge of which cell that is. Test only what is known.
        (let [result (nth (iterate dijkstra/step initial-values) 3)]
          (is (= 3 (- (count (::dijkstra/unvisited initial-values))
                      (count (::dijkstra/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (empty? (filter (::dijkstra/unvisited result)
                              (take 3 expected-cells)))
              "first three cells along the solution path should be visited")
          (is (= 6 (count (filter
                            (fn [[k v]] (> infinite-distance v))
                            (::dijkstra/distances result))))
              "six cells should have at least provisional distances")
          ; At this point, the frontier cells should each have a provisional
          ; distance of 3
          (is (every? (partial = 3)
                      (map (fn [cell] (get (::dijkstra/distances result) cell))
                           (filter (::dijkstra/unvisited result)
                                   (grid/linked-cells grid (grid/find-cell grid 1 1)))))))))))

(deftest test-solve
  ; TODO: test the full solution
  )

