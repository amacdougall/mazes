(ns mazes.algorithms.dijkstra
  "An implementation of Dijkstra's algorithm for maze solving."
  (:require [mazes.grid :as g]
            [clojure.set :refer [union difference]]
            [clojure.spec :as spec]))

(spec/def ::current ::g/cell)
(spec/def ::destination ::g/cell)
(spec/def ::distance int?)
(spec/def ::unvisited (spec/coll-of ::g/cell :kind set?))
(spec/def ::distances (spec/map-of ::g/cell ::distance :conform-keys true))
(spec/def ::step-values (spec/keys :req [::g/grid ::unvisited ::distances ::current]))

(def infinite-distance #?(:clj Integer/MAX_VALUE, :cljs js/Infinity))

(defn get-initial-values
  "Given a grid and an origin, sets up initial values for a single step in the
  algorithm."
  [grid origin]
  (let [all-cells (g/all-cells grid)
        ; origin cell is 0 steps from origin, of course; all others start at infinity
        initial-distance (fn [c] (if (= c origin) 0 infinite-distance))]
    {::g/grid grid
     ::unvisited (set all-cells)
     ::distances (zipmap all-cells (map initial-distance all-cells))
     ::current origin}))
(spec/fdef get-initial-values
  :args (spec/cat :grid ::g/grid :origin ::g/cell)
  :ret ::step-values)

(defn step
  "Given a working set of data for Dijkstra's algorithm, performs a single step
  in the iterative solution. If you only need the solution, use the solve
  function."
  ;; To update distances, we make a collection of updates: if (assoc m k v)
  ;; updates a single key-value, (apply assoc m [k v, ...]) applies all the
  ;; key-value updates in the vector. The distances of the neighbors, in our
  ;; uniform grid, are always 1 greater than the distance to the current
  ;; square. But we only want to perform the update if the new distance is less
  ;; than the old; hence the (min).
  ;;
  ;; To choose a new ::current, we get the key of the map entry which had the
  ;; lowest value in the distances map.
  [{:keys [::g/grid ::unvisited ::distances ::current] :as step-values}]
  (let [distance-updates ; a seq of [k v] pairs
        (map (fn [cell]
               (let [old-distance (distances cell)
                     new-distance (inc (distances current))]
                 [cell (min old-distance new-distance)]))
             (g/linked-cells grid current))
        distances (apply assoc distances (flatten distance-updates))
        unvisited (disj unvisited current)]
    {::g/grid grid
     ::unvisited unvisited
     ::distances distances
     ::current (key (apply min-key val (select-keys distances unvisited)))}))
(spec/fdef step
  :args (spec/cat :values ::step-values)
  :ret ::step-values)

(defn solve
  "Given a grid and start and end cells, uses Dijkstra's algorithm to produce a
  map of [::g/cell int?] map-entries; e.g. {<cell> <int>, ...}."
  ;; Implemented by repeatedly executing the step function in a loop.
  [grid origin destination]

  )

(defn path
  "Given a grid and start and end cells, uses Dijkstra's algorithm to produce a
  collection of ::g/direction items showing the most efficient route from the
  start cell to the end cell."
  [grid start end]

  )
