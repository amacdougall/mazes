(ns mazes.web.handlers
    (:require [re-frame.core :as re-frame]
              [mazes.algorithms.sidewinder :as sidewinder]
              [mazes.algorithms.dijkstra :as d]
              [mazes.grid :as g]
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
    (let [maze (sidewinder/generate (g/create-grid columns rows))]
      (assoc db :grid maze :solution nil))))

(re-frame/reg-event-db
  :solve-maze
  (fn [{grid :grid :as db} _]
    (let [origin (g/find-cell grid 0 0)
          destination (g/find-cell grid (dec (g/column-count grid)) (dec (g/row-count grid)))
          distances (:distances (d/solve grid origin destination))
          path (d/path grid origin destination distances)
          cells-on-path (g/cells-on-path grid origin path)
          path (partition 2 (interleave cells-on-path path))]
      (assoc db :solution {:distances distances
                           :path path}))))

(re-frame/reg-event-db
  :reset-solution
  (fn [db _]
    (assoc db :solution nil)))
