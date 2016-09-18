(ns mazes.web.handlers
    (:require [re-frame.core :as re-frame]
              [mazes.algorithms.sidewinder :as sidewinder]
              [mazes.grid :as grid]
              [mazes.web.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/app-db))

; Updates the supplied key in the app db.
(re-frame/reg-event-db
  :update
  (fn [db [_ k v]]
    (assoc db k v)))

(re-frame/reg-event-db
  :generate-maze
  (fn [{:keys [columns rows] :as db} [_ _]]
    (let [maze (sidewinder/generate (grid/create-grid columns rows))]
      (assoc db :grid maze))))
