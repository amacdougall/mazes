(ns mazes.algorithms.binary-tree
  "An implementation of the binary tree maze generation algorithm."
  (:require [mazes.grid :as g]
            [clojure.spec :as s]))

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (reduce
    (fn [grid cell]
      (let [open-directions (filter (partial g/move grid cell) [::g/s ::g/e])]
        (if (empty? open-directions)
          grid
          (g/link grid cell (rand-nth open-directions)))))
    grid
    (g/all-cells grid)))
(s/fdef generate
  :args (s/cat :grid ::g/grid)
  :ret ::g/grid)
