(ns mazes.renderers.svg.dijkstra
  "Provides an implementation of mazes.renderers.core/render-cell which applies
  additional formatting for solutions to Dijkstra's Algorithm."
  (:require [mazes.algorithms.dijkstra :refer [infinite-distance] :as d]
            [mazes.renderers.core :refer [render-cell]]
            [mazes.renderers.svg.core :as svg]))

(defn- cell-has-distance? [cell distances]
  (and (contains? distances cell)
       (> infinite-distance (get distances cell))))

(def path-highlight
  {:rect-attributes {:fill "green", :stroke "green"}})

;; NOTE: this expects a render-env whose (-> env :annotations :path) is a
;; sequence of [::g/cell ::g/direction] pairs. Until dijkstra/solve work is
;; complete, we must construct this path ourselves when building the render
;; environment; or more accurately, when constructing the :solution value in
;; the re-frame app-db.
(defmethod render-cell :dijkstra
  [{{distances ::d/distances, path ::d/path} :annotations :as render-env} cell]
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
