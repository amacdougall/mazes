(ns mazes.pathfinders.dijkstra
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
(spec/def ::path-step (spec/tuple ::g/cell ::g/direction))
(spec/def ::path (spec/coll-of ::path-step :kind sequential?))

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
     ::current (if (empty? unvisited)
                 nil
                 (key (apply min-key val (select-keys distances unvisited))))}))
(spec/fdef step
  :args (spec/cat :values ::step-values)
  :ret ::step-values)

;; True if unvisited set contains no reachable cells, based on distances map.
(defn- all-cells-visited? [unvisited distances]
  (or (empty? unvisited)
      (every? (partial = infinite-distance)
              (vals (select-keys distances unvisited)))) )

(defn compute-path
  "Given a grid, an origin cell, a destination cell, and a distance map
  produced by the solve function, returns a collection of ::path-step items
  showing the most efficient route from the start cell to the end cell. If
  there is no path from the origin to the destination, returns nil.
  
  This path is included for free in the result of the solve function, but it
  may be useful to use this function when interpreting the results of the step
  function, as in an interactive visualization."
  [grid origin destination distances]
  (if (= infinite-distance (get distances destination))
    nil
    (loop [cell destination, path []]
      (if (= cell origin)
        (reverse (map g/converse-directions path))
        ; among neighbors, find direction which has the lowest distance in distances
        (let [exits (::g/exits cell)
              ; a map of cells to exits
              neighbors (zipmap (map (partial g/move grid cell) exits) exits)
              next-cell (key (apply min-key val (select-keys distances (keys neighbors))))
              direction (neighbors next-cell)]
          (recur next-cell (conj path direction)))))))
(spec/fdef solve
  :args (spec/cat :grid ::g/grid :origin ::g/cell :destination (spec/? (spec/nilable ::g/cell)))
  :ret ::path)

(defn solve
  "Given a grid and an origin cell, uses Dijkstra's algorithm to generate a map
  with the following keys:

  ::distances - a map of the distances of each cell from the origin cell, in the
  form {<cell> <int>, ...}. This map is guaranteed to have a distance for every
  reachable cell.

  ::unvisited - the set of cells which remain unvisited after the solution has
  been produced.

  Given a grid, an origin cell, and a destination cell, returns this map, with
  the following keys:

  ::distances - a distance map which is guaranteed to contain the shortest
  distance for the destination cell, but may not have distances for any other
  cells.

  ::unvisited - the set of cells which remain unvisited after the solution has
  been produced.

  ::path - a sequence of ::grid/direction keywords, representing the directions
  to follow.

  ::path-steps - a sequence of ::grid/path-step tuples showing the steps along
  the path. The final step will have a nil direction.

  In either case, the distance map may contain unreachable cells. Check for the
  mazes.pathfinders.dijkstra/infinite-distance value."
  ([grid origin]
   (solve grid origin nil))
  ([grid origin destination]
   ;; NOTE: If all remaining cells have infinite distance, they must have been
   ;; unreachable.
   (let [complete? (if (nil? destination)
                     ; true when all reachable cells have been visited
                     all-cells-visited?
                     ; true when the destination cell has been visited, or all
                     ; reachable cells have been visited
                     (fn [unvisited distances]
                       (or (not (contains? unvisited destination))
                           (all-cells-visited? unvisited distances))))]
     (loop [{:keys [::distances ::unvisited] :as values} (get-initial-values grid origin)]
       (if (complete? unvisited distances)
         (let [solution {::distances distances ::unvisited unvisited}]
           ; if destination was not needed or not found, return only distances;
           ; otherwise, compute a solution path.
           (if (or (nil? destination)
                   (contains? unvisited destination))
             solution
             (let [path (compute-path grid origin destination distances)]
               (assoc solution ::path path ::path-steps (g/path-with-cells grid origin path)))))
         (recur (step values)))))))
(spec/fdef solve
  :args (spec/cat :grid ::g/grid :origin ::g/cell :destination (spec/? (spec/nilable ::g/cell)))
  :ret ::distances)

