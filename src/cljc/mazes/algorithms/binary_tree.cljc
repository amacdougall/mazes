(ns mazes.algorithms.binary-tree
  "An implementation of the binary tree maze generation algorithm."
  (:require [mazes.algorithms.core :as a]
            [mazes.grid :as g]
            [clojure.spec :as spec]))

(defmethod a/step-values ::binary-tree [_]
  (spec/keys :req [::a/algorithm ::g/grid ::g/cell]))

(spec/def ::a/step-values (spec/multi-spec a/step-values ::a/algorithm))

(defn initial-values
  "Given a grid, returns a ::a/step-values representing the initial state of the
  generation."
  [grid]
  {::a/algorithm ::binary-tree
   ::g/grid grid
   ::g/cell (g/find-cell grid [0 0])})
(spec/fdef initial-values
  :args (spec/cat :grid ::g/grid)
  :ret ::a/step-values)

(defn step
  "Given a ::a/step-values, executes one step of the binary tree algorithm and
  returns a new ::a/step-values representing the new state of the algorithm."
  [{:keys [::g/grid ::g/cell] :as step-values}]
  (let [open-directions (filter (partial g/move grid cell) [::g/s ::g/e])]
    (assoc step-values
           ::g/cell (g/next-cell grid cell)
           ::g/grid (if (empty? open-directions)
                      grid
                      (g/link grid cell (rand-nth open-directions))))))
(spec/fdef step
  :args (spec/cat :step-values ::a/step-values)
  :ret ::a/step-values)

(defn complete?
  "Given a ::a/step-values, returns true if the maze generation is complete."
  [step-values]
  (nil? (::g/cell step-values)))

(defn result
  "Given a ::a/step-values, returns the final result of the maze generation; in
  this case, a grid."
  [step-values]
  (::g/grid step-values))

(defn generate
  "Given a grid, returns a new grid with its cells linked to create a perfect
  maze."
  [grid]
  (loop [step-values (initial-values grid)]
    (if (complete? step-values)
      (result step-values)
      (recur (step step-values)))))
(spec/fdef generate
  :args (spec/cat :grid ::g/grid)
  :ret ::g/grid)
