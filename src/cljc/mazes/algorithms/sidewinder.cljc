(ns mazes.algorithms.sidewinder
  "An implementation of the sidewinder maze generation algorithm."
  (:require [mazes.grid :as grid]
            [clojure.spec :as spec]))

(def branch-chance 0.5)

(defn sidewinder
 ([grid run cell]
  (if (nil? cell)
    grid ; reached end of grid; return the completed maze
    (let [run (conj run cell)
          at-east-edge (nil? (grid/move grid cell ::grid/e))
          at-south-edge (nil? (grid/move grid cell ::grid/s))
          should-close (or at-east-edge
                           (and (not at-south-edge) (> branch-chance (rand))))]
      (cond
        ; if this run should close, link a random cell south and begin a new run
        should-close (recur (grid/link grid (rand-nth run) ::grid/s) [] (grid/next-cell grid cell))
        ; link east, add this cell to the run, and continue
        :else (recur (grid/link grid cell ::grid/e) run (grid/next-cell grid cell)))))))
(spec/fdef sidewinder
  :args (spec/cat :grid ::grid/grid :run vector? :cell ::grid/cell)
  :ret ::grid/grid)

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (sidewinder grid [] (grid/find-cell grid 0 0)))
(spec/fdef generate
  :args (spec/cat :grid ::grid/grid)
  :ret ::grid/grid)
