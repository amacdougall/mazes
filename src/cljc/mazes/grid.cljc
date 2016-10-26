(ns mazes.grid
  "Basic grid structure for maze wrangling."
  (:require [clojure.spec :as spec]
            [com.rpl.specter :as s]
            #?(:clj [com.rpl.specter.macros :as sm]))
  #?(:cljs (:require-macros [com.rpl.specter.macros :as sm])))

(spec/def ::x integer?)
(spec/def ::y integer?)
(spec/def ::width integer?)
(spec/def ::height integer?)
(spec/def ::coordinates (spec/tuple integer? integer?))
(spec/def ::direction #{::n ::ne ::e ::se ::s ::sw ::w ::nw})

;; Sufficient given a uniform grid.
(spec/def ::exits (spec/coll-of ::direction :kind set?))

(spec/def ::cell (spec/keys :req [::x ::y ::exits]))
(spec/def ::row (spec/coll-of ::cell))
(spec/def ::grid (spec/coll-of ::row))

;; Offsets by direction; add to a cell's coordinates to move.
(def translations
  {::n [0 -1]
   ::ne [1 -1]
   ::e [1 0]
   ::se [1 1]
   ::s [0 1]
   ::sw [-1 1]
   ::w [-1 0]
   ::nw [-1 -1]})

;; When opening an exit in a direction, open a complementary exit from the
;; destination to maintain grid geometry.
(def converse-directions
  {::n ::s
   ::ne ::sw
   ::e ::w
   ::se ::nw
   ::s ::n
   ::sw ::ne
   ::w ::e
   ::nw ::se})

(defn column-count
  "Given a grid, returns the number of columns in the grid."
  [grid]
  (count (first grid)))
(spec/fdef column-count
  :args (spec/cat :grid ::grid)
  :ret int?)

(defn row-count
  "Given a grid, returns the number of rows in the grid."
  [grid]
  (count grid))
(spec/fdef row-count
  :args (spec/cat :grid ::grid)
  :ret int?)

(defn grid-contains?
  "True if the supplied x and y coordinates fall within the grid boundaries."
  [grid x y]
  (and (<= 0 y)
       (<= 0 x)
       (< y (count grid))
       (< x (count (nth grid y)))))
(spec/fdef grid-contains?
  :args (spec/cat :grid ::grid :x ::x :y ::y)
  :ret boolean?)

(defn create-cell
  "Given x and y coordinates, returns a cell with x and y properties and an
  empty exits hash."
  [x y]
  {::x x, ::y y, ::exits #{}})
(spec/fdef create-cell
  :args (spec/cat :x ::x :y ::y)
  :ret ::cell)

(defn find-cell
  "Given a grid and x and y coordinates, returns the cell at that location in
  the grid. Returns nil if the coordinates are out of bounds."
  [grid x y]
  (if (grid-contains? grid x y)
    (-> grid (nth y) (nth x))
    nil))
(spec/fdef find-cell
  :args (spec/cat :grid ::grid :x ::x :y ::y)
  :ret (spec/nilable ::cell))

(defn random-cell
  "Given a grid, returns a random cell."
  [grid]
  (find-cell grid (rand-int (dec (column-count grid))) (rand-int (dec (row-count grid)))))
(spec/fdef random-cell
  :args (spec/cat :grid ::grid)
  :ret ::cell)

(defn all-cells
  "Given a grid, returns a sequence of all cells in the grid."
  [grid]
  (flatten grid))
(spec/fdef all-cells
  :args (spec/cat :grid ::grid)
  :ret (spec/coll-of ::cell))

(defn create-grid
  "Returns a grid of unconnected cells with the supplied number of columns
  (i.e. width) and rows (i.e. height)."
  [width height]
  (->> (for [y (range 0 height) x (range 0 width)] (create-cell x y))
    (partition width)
    (mapv (partial into []))))
(spec/fdef create-grid
  :args (spec/cat :width integer? :height integer?)
  :ret ::grid)

;; Returns the cell in the supplied direction, or nil.
(defn move [grid cell direction]
  (let [{ax ::x ay ::y} cell
        [bx by] (map + [ax ay] (direction translations))]
    (find-cell grid bx by)))
(spec/fdef move
  :args (spec/cat :grid ::grid :cell ::cell :direction ::direction)
  :ret (spec/nilable ::cell))

(defn next-cell
  "Given a grid and a cell, returns the next cell in the row, the first cell in
  the following row, or nil."
  [grid cell]
  (or (move grid cell ::e)
      (find-cell grid 0 (inc (::y cell)))
      nil))
(spec/fdef next-cell
  :args (spec/cat :grid ::grid :cell ::cell)
  :ret (spec/nilable ::cell))

(defn has-exit? [cell direction]
  (contains? (::exits cell) direction))
(spec/fdef has-exit?
  :args (spec/cat :cell ::cell :direction ::direction)
  :ret boolean?)

(defn linked-cells
  "Given a grid and a cell, returns a vector of all the reachable neighboring
  cells."
  [grid cell]
  (mapv (partial move grid cell) (::exits cell)))

;; Returns the cell, with the supplied exit added.
(defn- add-exit [cell direction]
  (update cell ::exits conj direction))

;; A Specter path to the supplied cell.
(defn- path-to [cell]
  [(s/keypath (::y cell)) (s/keypath (::x cell))])

(defn link
  "Given a grid, a cell, and a direction, returns a new grid modified to have a
  bidirectional link from the cell to its neighbor. If there can be no exit in
  that direction, returns the unmodified grid."
  [grid cell direction]
  (if-let [neighbor (move grid cell direction)]
    (->> grid
      (sm/transform (path-to cell)
                    #(add-exit % direction))
      (sm/transform (path-to neighbor)
                    #(add-exit % (direction converse-directions))))
    ; if neighbor was not found, return unchanged
    grid))
(spec/fdef link
  :args (spec/cat :grid ::grid :cell ::cell :direction ::direction)
  :ret ::grid)

(defn link-path
  "Given a grid, a starting cell, and a sequence of directions, links up a path
  through the maze using those directions. Makes no provision for going out of
  bounds or creating cycles in the maze graph, so beware!"
  [grid cell path]
  (if (empty? path)
    grid
    (recur (link grid cell (first path))
           (move grid cell (first path))
           (rest path))))
(spec/fdef link-path
  :args (spec/cat :grid ::grid :cell ::cell :path (spec/coll-of ::direction))
  :ret ::grid)

(defn cells-on-path
  "Given a grid, a starting cell, and a sequence of directions, returns a
  vector of the cells which lie along that path, regardless of whether they are
  linked by exits or not. Use link-path if you wish to ensure that they are
  linked."
  ([grid cell path]
   (cells-on-path grid cell path []))
  ([grid cell path result]
   (if (empty? path)
     (vec (conj result cell)) ; add current cell and return
     (recur grid
            (move grid cell (first path))
            (rest path)
            (conj result cell)))))
(spec/fdef cells-on-path
  :args (spec/cat :grid ::grid :cell ::cell :path (spec/coll-of ::direction)
                  :result (spec/? (spec/coll-of ::cell))))
