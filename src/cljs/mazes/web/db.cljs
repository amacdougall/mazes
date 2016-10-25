(ns mazes.web.db
  (:require [mazes.renderers.svg :as svg]))

(def app-db
  (merge
    ; svg rendering parameters
    svg/default-render-environment-options
    ; grid generation parameters
    {:columns 4
     :rows 4
     :grid nil
     :algorithm :sidewinder ; maze generation algorithm
     :annotation ; current maze annotation: renderer may choose to display it
     ;; NOTE: currently no standard for annotation types. The renderer just has
     ;; to deal with it.
     }))
