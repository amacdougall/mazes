(ns mazes.algorithms.binary-tree
  "An implementation of the binary tree maze generation algorithm."
  (:require [mazes.grid :as grid]
            [clojure.spec :as s]))

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (reduce
    (fn [grid cell]
      (let [open-directions (filter (partial grid/move grid cell) [::grid/s ::grid/e])]
        (if (empty? open-directions)
          grid
          (grid/link grid cell (rand-nth open-directions)))))
    grid
    (grid/all-cells grid)))
(s/fdef generate
  :args (s/cat :grid ::grid/grid)
  :ret ::grid/grid)
