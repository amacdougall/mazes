(ns mazes.web.subs
    (:require [re-frame.core :as re-frame]
              [mazes.renderers.svg :as svg])
    (:require-macros [reagent.ratom :refer [reaction]]))

(re-frame/reg-sub :columns (fn [db] (:columns db)))
(re-frame/reg-sub :rows (fn [db] (:rows db)))
(re-frame/reg-sub :width (fn [db] (:width db)))
(re-frame/reg-sub :height (fn [db] (:height db)))
(re-frame/reg-sub :size-spacing-ratio (fn [db] (:size-spacing-ratio db)))

(re-frame/reg-sub :grid (fn [db] (:grid db)))

; Returns a valid SVG render environment based on the current db state; or nil,
; if no grid exists in the db yet.
(re-frame/reg-sub
  :svg-render-environment
  (fn [db]
    (when (:grid db)
      (svg/render-environment
        (:grid db)
        (select-keys db (keys svg/default-render-environment-options))))))
