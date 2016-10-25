(ns mazes.renderers.svg.dijkstra
  "Provides an implementation of mazes.renderers.core/render-cell which applies
  additional formatting for solutions to Dijkstra's Algorithm."
  (:require [mazes.algorithms.dijkstra :refer [infinite-distance]]
            [mazes.renderers.core :refer [render-cell]]
            [mazes.renderers.svg.core :as svg]))

(defn- cell-has-distance? [cell distances]
  (and (contains? distances cell)
       (> infinite-distance (get distances cell))))

(def path-highlight
  {:rect-attributes {:fill "green", :stroke "green"}})

(defmethod render-cell :dijkstra
  [{{distances :distances, path :path} :annotations :as render-env} cell]
  (let [has-distance (and (not (nil? distances)) (contains? distances cell))
        on-path (and (not (nil? path)) (some (partial = cell) (map first path)))
        render-env (merge render-env (when on-path path-highlight))
        output (svg/render-cell render-env cell)
        {:keys [x y width height]} (svg/attributes (svg/find-rect output))]
    (if has-distance
      (conj output [:text {:x (+ (/ width 2) x)
                           :y (+ (/ height 2) y)
                           :font-size (str (/ width 30) "em")
                           :font-weight "bold"
                           :text-anchor "middle" :dominant-baseline "middle"
                           :fill "white"}
                    (if (= infinite-distance (get distances cell))
                      "-"
                      (get distances cell))])
      output)))
