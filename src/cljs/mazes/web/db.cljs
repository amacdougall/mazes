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
     ; a maze solution (currently only supports Dijkstra's algorithm)
     :solution nil
     :render-solution true
     ; we're throwing some ui state in here so it survives past Figwheel refreshes
     :selected-controls-tab :maze}))
