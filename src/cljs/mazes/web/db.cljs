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
     ;   :path - A sequence of directions from the origin to the destination.
     ;   :distances - A map of {<cell> <int>, ...}.
     ;   :grid - The grid upon which to operate.
     ;   :unvisited - A set of unvisited cells.
     ;   :current - The current cell being considered by the algorithm.
     :solution nil
     :render-solution true
     ; we're throwing some ui state in here so it survives past Figwheel refreshes
     :selected-controls-tab :maze}))
