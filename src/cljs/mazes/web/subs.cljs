(ns mazes.web.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :columns (fn [db] (:columns db)))
(re-frame/reg-sub :rows (fn [db] (:rows db)))
(re-frame/reg-sub :width (fn [db] (:width db)))
(re-frame/reg-sub :height (fn [db] (:height db)))
