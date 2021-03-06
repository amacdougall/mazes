(ns mazes.web.handlers
    (:require [re-frame.core :as re-frame]
              [mazes.generators.sidewinder :as sidewinder]
              [mazes.generators.aldous-broder :as aldous-broder]
              [mazes.generators.wilson :as wilson]
              [mazes.pathfinders.dijkstra :as d]
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
  (fn [{:keys [::g/columns ::g/rows] :as db} [_ _]]
    (.log js/console "Generating maze %d x %d")
    (let [maze (wilson/generate (g/create-grid columns rows))]
      (.log js/console "Maze: " (pr-str maze))
      (assoc db ::g/grid maze ::d/solution nil))))

(re-frame/reg-event-db
  :add-random-link
  (fn [{grid ::g/grid :as db} _]
    (let [cell (g/random-cell grid)
          directions (remove (::g/exits cell) [::g/n ::g/ne ::g/e ::g/se ::g/s ::g/sw ::g/w ::g/nw])]
      (assoc db ::g/grid (g/link grid cell (rand-nth directions))))))

(re-frame/reg-event-db
  :step-solution
  (fn [{:keys [::g/grid ::d/solution] :as db} _]
    (let [origin (g/find-cell grid [0 0])
          destination (g/find-cell grid [(dec (g/column-count grid))
                                         (dec (g/row-count grid))])]
      (cond
        ; if not even a partial solution exists, begin one)
        (nil? solution) (assoc db ::d/solution (d/get-initial-values grid origin))
        ; if solution is already complete, no change
        (not (nil? (::d/path solution))) db
        ; if solution is in progress, step forward
        :else
        (let [{:keys [::d/unvisited ::d/distances] :as step-values} (d/step solution)]
          (if (not (contains? unvisited destination))
            ; if destination has been visited, solution is complete; add a path
            ; TODO: update this in a less ugly fashion
            (assoc db ::d/solution
                   (assoc step-values ::d/path-steps
                          (g/path-with-cells grid origin
                                             (d/compute-path grid origin destination distances))))
            ; if destination has not been visited, just register the step
            (assoc db ::d/solution step-values)))))))

(re-frame/reg-event-db
  :solve-maze
  (fn [{grid ::g/grid :as db} _]
    (assoc db
           ::d/solution
           (d/solve grid
                    (g/find-cell grid [0 0])
                    (g/find-cell grid [(dec (g/column-count grid))
                                       (dec (g/row-count grid))])))))

(re-frame/reg-event-db
  :reset-solution
  (fn [db _]
    (assoc db ::d/solution nil)))
