(ns mazes.core
  "Basic tools for maze wrangling."
  (:require [clojure.spec :as spec]
            [com.rpl.specter.macros :refer [transform]]
            [com.rpl.specter :as s]))

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

(defn find-cell
  "Given a grid and x and y coordinates, returns the cell at that location in
  the grid. Returns nil if the coordinates are out of bounds."
  [grid x y]
  (if (grid-contains? grid x y)
    (-> grid (nth y) (nth x))
    nil))

(defn random-cell
  "Given a grid, returns a random cell."
  [grid]
  (find-cell grid (rand-int (count grid)) (rand-int (count (first grid)))))

(defn all-cells
  "Given a grid, returns a sequence of all cells in the grid."
  [grid]
  (flatten grid))

(defn create-grid
  "Returns a grid of unconnected cells with the supplied number of columns
  (i.e. width) and rows (i.e. height)."
  [width height]
  (->> (for [y (range 0 height) x (range 0 width)] (create-cell x y))
    (partition width)
    (mapv (partial into []))))


;; Returns the cell in the supplied direction, or nil.
(defn- move [grid cell direction]
  (let [{ax ::x ay ::y} cell
        [bx by] (map + [ax ay] (direction translations))]
    (find-cell grid bx by)))

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
      (transform (path-to cell)
                 #(add-exit % direction))
      (transform (path-to neighbor)
                 #(add-exit % (direction converse-directions))))
    ; if neighbor was not found, return unchanged
    grid))
