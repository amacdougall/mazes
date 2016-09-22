(ns mazes.helpers)

(defn deep-merge
  "Deeply merges maps so that nested maps are combined rather than replaced.
  Ignores nils.

  For example:

  (deep-merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:bar :baz, :fuzz :buzz}}
  ;; contrast with clojure.core/merge
  (merge {:foo {:bar :baz}} {:foo {:fuzz :buzz}})
  ;;=> {:foo {:fuzz :quzz}} ; note how last value for :foo wins"
  [& vs]
  (if (every? map? vs)
    (apply merge-with deep-merge vs)
    (if (and (map? (first vs)) (empty? (remove nil? (rest vs))))
      ; attempting to merge nil into a map; behave like clojure.core/merge and
      ; return base map
      (first vs)
      ; presumably we recurred down to a scalar; return the scalar
      (last vs))))

(defn deep-merge-with
  "Deeply merges like `deep-merge`, but uses `f` to produce a value from the
  conflicting values for a key in multiple maps."
  [f & vs]
  (if (every? map? vs)
    (apply merge-with (partial deep-merge-with f) vs)
    (apply f vs)))
