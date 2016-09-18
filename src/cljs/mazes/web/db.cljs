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
     ; TODO: allow algorithm and params change
     :algorithm :sidewinder}))
