(ns mazes.algorithms.sidewinder
  "An implementation of the sidewinder maze generation algorithm."
  (:require [mazes.grid :as g]
            [clojure.spec :as spec]))

(def branch-chance 0.5)

(defn sidewinder
 ([grid run cell]
  (if (nil? cell)
    grid ; reached end of grid; return the completed maze
    (let [run (conj run cell)
          at-east-edge (nil? (g/move grid cell ::g/e))
          at-south-edge (nil? (g/move grid cell ::g/s))
          should-close (or at-east-edge
                           (and (not at-south-edge) (> branch-chance (rand))))]
      (cond
        ; if this run should close, link a random cell south and begin a new run
        should-close (recur (g/link grid (rand-nth run) ::g/s) [] (g/next-cell grid cell))
        ; link east, add this cell to the run, and continue
        :else (recur (g/link grid cell ::g/e) run (g/next-cell grid cell)))))))
(spec/fdef sidewinder
  :args (spec/cat :grid ::g/grid :run vector? :cell ::g/cell)
  :ret ::g/grid)

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (sidewinder grid [] (g/find-cell grid [0 0])))
(spec/fdef generate
  :args (spec/cat :grid ::g/grid)
  :ret ::g/grid)
