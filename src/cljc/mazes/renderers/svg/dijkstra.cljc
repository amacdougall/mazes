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
  {:rect-attributes {:fill "forestgreen", :stroke "forestgreen"}})

(def current-highlight
  {:rect-attributes {:fill "cadetblue", :stroke "cadetblue"}})

(def unvisited-highlight
  {:rect-attributes {:fill "gray", :stroke "gray"}})

;; NOTE: this expects a render-env whose (-> env :annotations :path) is a
;; sequence of [::g/cell ::g/direction] pairs. Until dijkstra/solve work is
;; complete, we must construct this path ourselves when building the render
;; environment; or more accurately, when constructing the :solution value in
;; the re-frame app-db.
(defmethod render-cell :dijkstra
  [{{:keys [::d/distances ::d/path-steps ::d/current ::d/unvisited]} :annotations :as render-env} cell]
  (let [has-distance (and (not (nil? distances)) (contains? distances cell))
        is-current (= cell current)
        is-unvisited (contains? unvisited cell)
        on-path (and (not (nil? path-steps)) (some (partial = cell) (map first path-steps)))
        render-env (merge render-env
                          (when is-unvisited unvisited-highlight)
                          (when is-current current-highlight)
                          (when on-path path-highlight))
        output (svg/render-cell render-env cell)
        {:keys [x y width height]} (svg/attributes (:rect output))]
    (if has-distance
      (assoc output :text [:text {:x (+ (/ width 2) x)
                                  :y (+ (/ height 2) y)
                                  :font-size (str (/ width 30) "em")
                                  :font-weight "bold"
                                  :text-anchor "middle" :dominant-baseline "middle"
                                  :fill "white"}
                           (if (= infinite-distance (get distances cell))
                             "-"
                             (get distances cell))])
      output)))
