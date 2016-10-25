(ns test.mazes.helpers)

(defn has-values?
  "True if the candidate map has every key-value pair defined in the exemplar map."
  [exemplar candidate]
  (every? (fn [k] (= (candidate k) (exemplar k))) (keys exemplar)))

;; True if the two collections contain the same numbers in the same order,
;; regardless of the types of the collections or numbers. Uses ==.
(defn equal-numbers? [c1 c2]
  (every? #(apply == %) (partition 2 (interleave c1 c2))))

;; NOTE: We're using a lot of this custom approximate-equality in these tests,
;; because we never know when something is going to return a double instead of
;; an int, and when division is involved, we find that this kind of check fails:
;; (== 2.0 1.9999999999999999999999) ...in defiance of our intent.
(defn ≈ [a b]
  (clojure.algo.generic.math-functions/approx= a b 1e-2))

