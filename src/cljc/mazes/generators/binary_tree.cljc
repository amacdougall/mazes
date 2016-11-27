(ns mazes.generators.binary-tree
  "An implementation of the binary tree maze generation algorithm."
  (:require [mazes.generators.core :as a]
            [mazes.grid :as g]
            [clojure.spec :as spec]))

(defmethod a/step-values ::binary-tree [_]
  (spec/keys :req [::a/algorithm ::g/grid ::g/cell]))

(spec/def ::a/step-values (spec/multi-spec a/step-values ::a/algorithm))

(defmethod a/initial-values ::binary-tree
  [grid algorithm]
  {::a/algorithm ::binary-tree
   ::g/grid grid
   ::g/cell (g/find-cell grid [0 0])})
(spec/fdef a/initial-values
  :args (spec/cat :grid ::g/grid :algorithm ::a/algorithm)
  :ret ::a/step-values)

(defmethod a/step ::binary-tree
  [{:keys [::g/grid ::g/cell] :as step-values}]
  (let [open-directions (filter (partial g/move grid cell) [::g/s ::g/e])]
    (assoc step-values
           ::g/cell (g/next-cell grid cell)
           ::g/grid (if (empty? open-directions)
                      grid
                      (g/link grid cell (rand-nth open-directions))))))
(spec/fdef a/step
  :args (spec/cat :step-values ::a/step-values)
  :ret ::a/step-values)

(defmethod a/complete? ::binary-tree
  [step-values]
  (nil? (::g/cell step-values)))

(defmethod a/result ::binary-tree
  [step-values]
  (::g/grid step-values))

(defmethod a/generate ::binary-tree
  [grid algorithm]
  (loop [step-values (a/initial-values grid algorithm)]
    (if (a/complete? step-values)
      (a/result step-values)
      (recur (a/step step-values)))))
(spec/fdef generate
  :args (spec/cat :grid ::g/grid)
  :ret ::g/grid)
