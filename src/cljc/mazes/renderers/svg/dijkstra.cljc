(ns mazes.renderers.svg.dijkstra
  "Provides an implementation of mazes.renderers.core/render-cell which applies
  additional formatting for solutions to Dijkstra's Algorithm."
  (:require [mazes.grid :as g]
            [mazes.algorithms.dijkstra :refer [infinite-distance] :as d]
            [mazes.renderers.core :refer [render-cell]]
            [mazes.renderers.svg.core :as svg]
            [thi.ng.color.core :as c]))

(defn- cell-has-distance? [cell distances]
  (and (contains? distances cell)
       (> infinite-distance (get distances cell))))

;; Since highlights may be based on parameters, all highlights are functions
;; instead of defs, for consistency. Otherwise we would have to remember which
;; highlights are functions, such as distance-highlight, and which are not.
(defn- path-highlight []
  {:rect-attributes {:fill "forestgreen", :stroke "forestgreen"}})

(defn- current-highlight []
  {:rect-attributes {:fill "cadetblue", :stroke "cadetblue"}})

(defn- unvisited-highlight []
  {:rect-attributes {:fill "gray", :stroke "gray"}})

(defn- distance-highlight [distance max-distance]
  (if (= infinite-distance distance)
    {:rect-attributes {:fill "gray", :stroke "gray"}}
    ; begin at 0 brightness and increase as we near the greatest known
    ; distance; this means that cells will fade as we explore the grid
    (let [distance-color @(-> (c/css "#ff0000")
                            ; adjust-brightness -1 removes all brightness
                            (c/adjust-brightness (- (/ distance max-distance) 1))
                            (c/as-css))]
      {:rect-attributes {:fill distance-color
                         :stroke distance-color}})
    )
  )

(def infinite-distance-text "-")

(defmethod render-cell :dijkstra
  [{{:keys [::d/distances ::d/path-steps ::d/current ::d/unvisited]} :annotations :as render-env} cell]
  (let [has-distance (and (not (nil? distances)) (contains? distances cell))
        max-distance (apply max (remove (partial = infinite-distance) (vals distances)))
        is-current (= cell current)
        on-path (and (not (nil? path-steps)) (some (partial = cell) (map first path-steps)))
        render-env (merge render-env
                          (when has-distance (distance-highlight (get distances cell)
                                                                 max-distance))
                          (when is-current (current-highlight))
                          (when on-path (path-highlight)))
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
                             infinite-distance-text
                             (get distances cell))])
      output)))
