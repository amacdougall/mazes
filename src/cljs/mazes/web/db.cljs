(ns mazes.web.db
  (:require [mazes.renderers.svg.core :as svg]))

(def app-db
  (merge
    ; svg rendering parameters
    svg/default-render-environment-options
    ; grid generation parameters
    {:columns 4
     :rows 4
     :grid nil
     ; maze generation algorithm (always sidewinder right now)
     :algorithm :sidewinder
     ; a complete or partial maze solution (currently only supports Dijkstra's algorithm),
     ; containing the following possible keys:
     ;   ::grid/grid - The grid upon which to operate.
     ;   ::dijkstra/path - A sequence of directions from the origin to the destination.
     ;   ::dijkstra/distances - A map of {<cell> <int>, ...}.
     ;   ::dijkstra/unvisited - A set of unvisited cells.
     ;   ::dijkstra/current - The current cell being considered by the algorithm.
     :solution nil
     :render-solution true
     ; we're throwing some ui state in here so it survives past Figwheel refreshes
     :selected-controls-tab :maze}))
