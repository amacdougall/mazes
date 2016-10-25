(ns mazes.web.handlers
    (:require [re-frame.core :as re-frame]
              [mazes.algorithms.sidewinder :as sidewinder]
              [mazes.algorithms.dijkstra :as dijkstra]
              [mazes.grid :as grid]
              [mazes.web.db :as db]
              [com.rpl.specter :as s])
    (:require-macros [com.rpl.specter.macros :as sm]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/app-db))

; Sets the value at the supplied keypath in the app db.
(re-frame/reg-event-db
  :update
  (fn [db [_ ks v]]
    (sm/setval ks v db)))

(re-frame/reg-event-db
  :generate-maze
  (fn [{:keys [columns rows] :as db} [_ _]]
    (let [maze (sidewinder/generate (grid/create-grid columns rows))
          solution (dijkstra/solve maze (grid/find-cell maze 0 0) (grid/find-cell maze (dec columns) (dec rows)))]
      (assoc db :grid maze :solution solution))))
