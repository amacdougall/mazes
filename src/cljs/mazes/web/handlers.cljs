(ns mazes.web.handlers
    (:require [re-frame.core :as re-frame]
              [mazes.web.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))
