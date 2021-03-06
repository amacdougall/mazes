(ns test.mazes.generators.aldous-broder-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as stest]
            [mazes.grid :as g]
            [mazes.generators.aldous-broder :as aldous-broder]
            [mazes.pathfinders.dijkstra :as d]))

(stest/instrument)

;; NOTE: Since the Aldous-Broder algorithm is a random walk, it has
;; indeterminate runtime, increasing rapidly with grid size. Going from 5x5 to
;; 6x6, for instance, increases runtime (on my dev hardware) from a split
;; second to a full second; 7x7 was taking between two and six seconds.
;;
;; Even if the algorithm is not implemented correctly, it can occasionally
;; stumble into a correct layout. Increasing the grid size makes this situation
;; vanishingly unlikely, but increasing it too far also makes the algorithm run
;; very slowly. 5x5 seemed like an acceptable compromise.
;;
;; The :slow metadata ties in with :test-selectors in project.clj. Add the
;; :slow param to lein test to run :slow tests specifically, or :all to run all
;; tests.
(deftest ^:slow test-generate
  (let [grid (g/create-grid 5 5)
        maze (aldous-broder/generate grid)]
    (is (spec/valid? ::g/grid maze))
    (let [origin (g/find-cell maze [0 0])
          destination (g/find-cell maze [(dec (g/row-count grid))
                                         (dec (g/column-count grid))])
          path (::d/path (d/solve maze origin destination))]
      (is (not (nil? path))
          "maze must have a path from the origin to the destination"))))
