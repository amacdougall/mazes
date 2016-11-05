;; NOTE: As with all the maze generation algorithms, we store grid cells as
;; coordinate tuples while we build the grid, because individual cell values
;; will quickly become obsolete as the algorithm creates successive versions of
;; the grid.
(ns mazes.algorithms.wilson
  "An implementation of Wilson's algorithm for maze generation."
  (:require [mazes.grid :as g]
            [clojure.set :refer [difference]]
            [clojure.spec :as spec]))

;; These defs resemble the Dijkstra ones, but are distinct.
(spec/def ::current ::g/coordinates)
(spec/def ::next (spec/nilable ::g/coordinates))
(spec/def ::unconnected (spec/coll-of ::g/coordinates :kind set?))
(spec/def ::path (spec/coll-of ::g/coordinates :kind vector?))
(spec/def ::step-values (spec/keys :req [::g/grid
                                         ::current
                                         ::next
                                         ::unconnected
                                         ::path]))

;; Pop items off the supplied collection until the head item matches the
;; predicate. If no item in the collection matches the predicate, returns an
;; empty collection (having fruitlessly rewound to the very beginning).
;;
;; Example: (rewind-to #{5} [1 2 3 4 5 6 7 8 9]) ; [1 2 3 4 5]
;;
;; Uses peek and pop under the hood, so this only makes sense with collection
;; types which have stack-like peek/pop behavior. Since the return value is
;; created with pop, this will preserve the type of the input collection.
(defn rewind-to [pred coll]
  (if (or (empty? coll) (pred (peek coll)))
    coll
    (rewind-to pred (pop coll))))
(spec/fdef rewind-to
  :args (spec/cat :pred ifn? :coll sequential?)
  :ret sequential?)

(defn connect-path
  [grid path]
  (if (= 1 (count path)) ; all pairs are connected
    grid
    (let [[origin destination] (map (partial g/find-cell grid) (take-last 2 path))
          direction (g/find-direction origin destination)]
      (recur (g/link grid origin direction) (pop path)))))

(defn connect-path-step
  "Given a ::step-values, updates the grid to link each ::path coordinate in
  turn, including ::next. Removes all linked coordinates from ::unconnected.
  Clears ::path."
  [{:keys [::g/grid ::current ::next ::unconnected ::path] :as step-values}]
  (assoc step-values
         ::g/grid (connect-path grid (conj path next))
         ::unconnected (clojure.set/difference unconnected path)
         ::path []))

(defn erase-loop-step
  "Given a ::step-values whose ::path contains a loop, returns the values with
  ::path rewinded to the most recent occurrence of the ::next coordinates. For
  instance, if the ::path is [a b c], and ::next is a, ::path will become [a].
  
  If the path does not in fact contain a loop, returns the step-values
  unchanged."
  [{:keys [::next ::path] :as step-values}]
  (if (some #{next} path)
    (assoc step-values ::path (rewind-to #{next} path))
    step-values))

(defn add-to-path-step
  "Given a ::step-values, returns the values with ::next appended to the path."
  [step-values]
  (update step-values ::path conj (::next step-values)))

(defn step-function
  "Given a ::step-values object, returns the appropriate step function to use
  for this iteration in the algorithm."
  [{:keys [::g/grid ::current ::next ::unconnected ::path] :as step-values}]
  (let [unconnected? (partial contains? unconnected)
        connected? (complement unconnected?)]
    (cond
      (and (connected? current) (connected? next)) identity
      (and (unconnected? current) (connected? next)) connect-path-step
      :else (if (some #{next} path)
              erase-loop-step
              add-to-path-step))))
(spec/fdef step-function
  :args (spec/cat :step-values ::step-values)
  :ret fn?)

(defn wilson
  "Performs a single step in Wilson's Algorithm."
  [{:keys [::g/grid ::current ::next ::unconnected] :as step-values}]
  (if (nil? next)
    ; choose a random exit from current cell and retry
    (let [current-cell (g/find-cell grid current)
          direction (g/random-exit grid current-cell)
          next-cell (g/move grid current-cell direction)]
      (recur (assoc step-values ::next (g/coordinates next-cell))))
    (let [next-cell (g/find-cell grid next)
          direction (g/random-exit grid next-cell)
          step (step-function step-values)]
      (assoc (step step-values)
             ::current next
             ::next (g/coordinates (g/move grid next-cell direction))))))
(spec/fdef wilson
  :args (spec/cat :step-values ::step-values)
  :ret ::step-values)

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (loop [step-values
         {::g/grid grid
          ::current (g/coordinates (g/random-cell grid))
          ::next nil
          ::unconnected (disj (set (map g/coordinates (g/all-cells grid))) [0 0])
          ::path []}]
    (if (empty? (::unconnected step-values))
      (::g/grid step-values)
      (recur (wilson step-values)))))
(spec/fdef generate
  :args (spec/cat :grid ::g/grid)
  :ret ::g/grid)
