(ns mazes.web.views
    (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
              [re-com.core :as re-com]
              [mazes.algorithms.sidewinder :as sidewinder]
              [mazes.grid :as grid]
              [mazes.renderers.svg :as svg]
              [mazes.web.handlers :as handlers]))

; TODO: CSS for everything

(defn maze-svg []
  (let [grid (subscribe [:grid])
        render-env (subscribe [:svg-render-environment])]
    (fn []
      (when (and @grid @render-env)
        (svg/render @render-env @grid)))))

(defn slider [k {:keys [min max]}]
  (let [value (subscribe [k])]
    (fn []
      [re-com/v-box
       :children
       [[re-com/label
         :label @value]
        [re-com/slider
         :style {:width "100%"}
         :model @value
         :min min
         :max max
         :on-change #(dispatch [:update k %])]]])))

(def rounded-panel
  (merge {:background-color "white"
          :border           "1px solid lightgray"
          :border-radius    "4px"
          :padding          "0px 20px 0px 20px"}))

(defn main-panel []
  (fn []
    [re-com/v-box
     :style {:background-color "lightgray" :padding "1rem"}
     :height "100%"
     :width "100%"
     :children
     [[re-com/title :label "Mazes", :level :level1]
      [re-com/h-split
       :width "100%"
       :initial-split "30%"
       :panel-1
       [re-com/v-box
        :style (merge rounded-panel
                      {:width "100%"})
        :gap "2rem"
        :children
        [[:h2 "Left Pane"]
         [slider :columns {:min 2 :max 20}]
         [slider :rows {:min 2 :max 20}]
         [slider :width {:min 100 :max 1000}]
         [slider :height {:min 100 :max 1000}]
         [re-com/button
          :label "Generate Maze"
          :on-click #(dispatch [:generate-maze])]]]
       :panel-2
       [:div {:style (merge rounded-panel
                            {:width "100%"
                             :margin-right "20px"})}
        [maze-svg]]
       ]]]))
