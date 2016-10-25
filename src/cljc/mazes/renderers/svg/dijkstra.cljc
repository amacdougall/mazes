(ns mazes.renderers.svg.dijkstra
  "Provides an implementation of mazes.renderers.core/render-cell which applies
  additional formatting for solutions to Dijkstra's Algorithm."
  (:require [mazes.renderers.core :refer [render-cell]]
            [mazes.renderers.svg.core :as svg]))

(defmethod render-cell :dijkstra
  [{{:keys [distances path]} :annotations :as render-env} cell]
  (svg/render-cell render-env cell))
