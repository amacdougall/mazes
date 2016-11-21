(ns mazes.algorithms.aldous-broder
  "An implementation of the Aldous-Broder maze generation algorithm."
  (:require [mazes.grid :as g]
            [clojure.spec :as spec]))

; NOTE: unvisited is a set of [x y] coordinate pairs!
(defn aldous-broder
  [grid cell unvisited]
  (if (empty? unvisited)
    grid ; visited all cells; return maze
    (let [direction (g/random-exit grid cell)
          next-cell (g/move grid cell direction)]
      (if (contains? unvisited (g/coordinates next-cell))
        ; link from current cell to next, and move in the random direction
        ; NOTE: we need an updated next-cell, since the grid changes
        (let [grid (g/link grid cell direction)
              next-cell (g/find-cell grid (g/coordinates next-cell))]
          (recur grid next-cell (disj unvisited (g/coordinates next-cell))))
        ; already visited the next cell; move without linking
        (recur grid (g/move grid cell direction) unvisited)))))
(spec/fdef aldous-broder
  :args (spec/cat :grid ::g/grid :cell ::g/cell :unvisited set?)
  :ret ::g/grid)

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (aldous-broder grid (g/find-cell grid [0 0]) (set (map g/coordinates (g/all-cells grid)))))
(spec/fdef generate
  :args (spec/cat :grid ::g/grid)
  :ret ::g/grid)
