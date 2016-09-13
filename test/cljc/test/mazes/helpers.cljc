(ns test.mazes.helpers)

(defn has-values?
  "True if the candidate map has every key-value pair defined in the exemplar map."
  [exemplar candidate]
  (every? (fn [k] (= (candidate k) (exemplar k))) (keys exemplar)))

;; True if the two collections contain the same numbers in the same order,
;; regardless of the types of the collections or numbers. Uses ==.
(defn equal-numbers? [c1 c2]
  (every? #(apply == %) (partition 2 (interleave c1 c2))))

