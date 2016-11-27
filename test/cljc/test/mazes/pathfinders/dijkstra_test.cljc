(ns test.mazes.pathfinders.dijkstra-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer [intersection]]
            [clojure.spec :as spec]
            [clojure.spec.test :as stest]
            [mazes.grid :as g]
            [mazes.pathfinders.dijkstra :as d]))

(stest/instrument)

(def get-initial-values #'mazes.pathfinders.dijkstra/get-initial-values)
(def infinite-distance #?(:clj Integer/MAX_VALUE, :cljs js/Infinity))

(deftest test-step
  (testing "simple maze"
    (let [grid (g/create-grid 2 2)
          path [::g/e ::g/s ::g/w]
          grid (g/link-path grid (g/find-cell grid [0 0]) path)
          grid (g/link grid (g/find-cell grid [1 1]) ::g/w)
          origin (g/find-cell grid [0 0])
          expected-cells (g/cells-on-path grid origin path)
          initial-values (get-initial-values grid origin)]
      (testing "first step"
        (let [result (d/step initial-values)]
          (is (= 1 (- (count (::d/unvisited initial-values))
                      (count (::d/unvisited result))))
              "unvisited set should shrink by one")
          (is (not (contains? (::d/unvisited result) origin))
              "origin cell should be visited")
          (is (= (::d/current result) (g/find-cell grid [1 0]))
              "northeast cell should be the new current")))
      (testing "second step"
        (let [result (nth (iterate d/step initial-values) 2)]
          (is (= 2 (- (count (::d/unvisited initial-values))
                      (count (::d/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (not (contains? (::d/unvisited result) origin))
              "origin cell should be visited")
          (is (not (contains? (::d/unvisited result)
                              (g/find-cell grid [1 0])))
              "cell east of origin should be visited")
          (is (= (::d/current result) (g/find-cell grid [1 1]))
              "southeast cell should be current")
          (is (= 2 (get (::d/distances result)
                        (::d/current result)))
              "distance to new current cell should be accurate")))))
  (testing "complex maze"
    (let [grid (g/create-grid 3 3)
          path [::g/e ::g/s ::g/e ::g/s ::g/e]
          grid (g/link-path grid (g/find-cell grid [0 0]) path)
          ; give the center cell exits in each other direction
          grid (g/link grid (g/find-cell grid [1 1]) ::g/w)
          grid (g/link grid (g/find-cell grid [1 1]) ::g/s)
          origin (g/find-cell grid [0 0])
          expected-cells (g/cells-on-path grid origin path)
          initial-values (get-initial-values grid origin)]
      (testing "at a branching intersection"
        (let [result (nth (iterate d/step initial-values) 2)]
          (is (= 2 (- (count (::d/unvisited initial-values))
                      (count (::d/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (empty? (filter (::d/unvisited result)
                              (take 2 expected-cells)))
              "first two cells along the solution path should be visited")
          (is (= (::d/current result) (g/find-cell grid [1 1]))
              "center cell should be current")))
      (testing "after the branching intersection"
        ; As implemented, the algorithm will choose the member of the unvisited
        ; set with the lowest distance. But since it is choosing from a set, we
        ; have no knowledge of which cell that is. Test only what is known.
        (let [result (nth (iterate d/step initial-values) 3)]
          (is (= 3 (- (count (::d/unvisited initial-values))
                      (count (::d/unvisited result))))
              "unvisited set should shrink by one per step")
          (is (empty? (filter (::d/unvisited result)
                              (take 3 expected-cells)))
              "first three cells along the solution path should be visited")
          (is (= 6 (count (filter
                            (fn [[k v]] (> infinite-distance v))
                            (::d/distances result))))
              "six cells should have at least provisional distances")
          ; At this point, the frontier cells should each have a provisional
          ; distance of 3
          (is (every? (partial = 3)
                      (map (fn [cell] (get (::d/distances result) cell))
                           (filter (::d/unvisited result)
                                   (g/linked-cells grid (g/find-cell grid [1 1])))))))))))

(defn- unreachable [distances]
  (filter (partial = infinite-distance) (vals distances)))

(deftest test-solve
  ; NOTE: I just plotted this test maze on paper. To understand this code,
  ; you should probably do the same.
  (let [grid (g/create-grid 4 4)
        path [::g/e ::g/s ::g/w ::g/s ::g/e ::g/e ::g/s ::g/e]
        grid (g/link-path grid (g/find-cell grid [0 0]) path)
        ; create a dead-end branch; many cells remain unreachable
        maze (g/link-path grid (g/find-cell grid [1 0]) [::g/e ::g/s ::g/e ::g/n])
        ; fill in the gaps to create a perfect maze
        p-maze (g/link maze (g/find-cell maze [3 1]) ::g/s)
        p-maze (g/link-path p-maze (g/find-cell p-maze [0 2]) [::g/s ::g/e])]
    (testing "with a target destination"
      (testing "solution properties"
        (let [origin (g/find-cell maze [0 0])
              destination (g/find-cell maze [3 3])
              solution (d/solve maze origin destination)]
          (is (contains? solution ::d/distances))
          (is (contains? solution ::d/unvisited))
          (is (contains? solution ::d/path))
          (is (contains? solution ::d/path-steps))))
      (testing "with unreachable cells"
        (let [origin (g/find-cell maze [0 0])
              destination (g/find-cell maze [3 3]) ; bottom right
              {:keys [::d/unvisited ::d/distances]} (d/solve maze origin destination)]
          (is (= 3 (count (unreachable distances)))
              "three cells should have been unreachable")
          (is (= infinite-distance (get distances (g/find-cell maze [0 3])))
              "bottom left cell should have been unreachable")
          (is (contains? unvisited (g/find-cell maze [0 3]))
              "unreachable cell should be in the unvisited set")
          (is (= 8 (get distances destination)))))
      (testing "with no unreachable cells"
        ; fill in the gaps
        (let [origin (g/find-cell p-maze [0 0])
              destination (g/find-cell p-maze [3 3])
              distances (::d/distances (d/solve p-maze origin destination))]
          (is (empty? (unreachable distances)))
          (is (= 8 (get distances destination)))))
      (testing "with a target destination that leaves cells unexplored"
        (let [origin (g/find-cell p-maze [0 0])
              destination (g/find-cell p-maze [2 2])
              {:keys [::d/unvisited ::d/distances]} (d/solve grid origin destination)]
          (is (= infinite-distance (get distances (g/find-cell grid [0 3])))
              "cell beyond the destination should be unvisited")
          (is (contains? unvisited (g/find-cell grid [0 3])))
          (is (= 6 (get distances destination)))))
      (testing "when destination cell is unreachable"
        (let [origin (g/find-cell grid [0 0])
              destination (g/find-cell grid [0 3]) ; should be unreachable
              solution (d/solve maze origin destination)
              {:keys [::d/distances ::d/unvisited ::d/path]} solution]
          (is (= infinite-distance (get distances destination)))
          (is (contains? unvisited destination))
          (is (nil? path)))))
    (testing "with no target destination"
      (let [origin (g/find-cell maze [1 1])
            {:keys [::d/unvisited ::d/distances]} (d/solve maze origin)]
        (is (= 3 (count (unreachable distances))))
        (is (= 3 (count unvisited)))
        (is (= 0 (get distances origin)))
        (is (= 1 (get distances (g/move maze origin ::g/n)))))
      (let [origin (g/find-cell p-maze [1 1])
            solution (d/solve p-maze origin)
            distances (::d/distances solution)
            path (::d/path solution)]
        (is (empty? (unreachable distances)))
        (is (nil? path))))))

; A single test should be sufficient: all logic except the distances->path
; translation is covered by the dijkstra/solve tests.
(deftest test-path
  (let [grid (g/create-grid 4 4)
        path [::g/e ::g/s ::g/w ::g/s ::g/e ::g/e ::g/s ::g/e]
        grid (g/link-path grid (g/find-cell grid [0 0]) path)
        ; add a dead end
        grid (g/link-path grid (g/find-cell grid [1 0]) [::g/e ::g/s ::g/e ::g/n])
        origin (g/find-cell grid [0 0])
        destination (g/find-cell grid [3 3])
        expected-cells (g/cells-on-path grid origin path)]
    (let [solved-path (::d/path (d/solve grid origin destination))
          actual-cells (g/cells-on-path grid origin solved-path)]
      (is (= path solved-path)
          "path direction list should be correct")
      (is (= expected-cells actual-cells)
          "cells along path should be correct")
      (is (= origin (first actual-cells))
          "origin should be the first cell")
      (is (= destination (last actual-cells))
          "destination should be the last cell"))))
