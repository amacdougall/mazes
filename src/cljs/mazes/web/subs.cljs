(ns mazes.web.subs
    (:require [re-frame.core :as re-frame]
              [mazes.renderers.svg.core :as svg])
    (:require-macros [reagent.ratom :refer [reaction]]))

(def simple-subscriptions
  [:columns
   :rows
   :width
   :height
   :size-spacing-ratio
   :rect-attributes
   :line-attributes
   :selected-controls-tab
   :grid])

(doseq [k simple-subscriptions]
  (re-frame/reg-sub k (fn [db] (k db))))

; Returns a valid SVG render environment based on the current db state; or nil,
; if no grid exists in the db yet. If the solution should be rendered, adds a
; Dijkstra annotation.
(re-frame/reg-sub
  :svg-render-environment
  (fn [db]
    (when (:grid db)
      (merge
        (svg/render-environment
          (:grid db)
          (select-keys db (keys svg/default-render-environment-options)))
        (when (:render-solution db)
          {:annotations (merge (:solution db) {:type :dijkstra})})))))
