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

;; Given a keypath such as [:top] or [:top :node :leaf], returns a slider which
;; alters this value. Real-world example: [:columns], which is at the top level
;; of the app db; [:rect-attributes :fill], which is one level down.
(defn slider [ks {:keys [label min max]}]
  (let [value (subscribe [(first ks)])
        target-value (fn [top] (get-in top (rest ks)))]
    (fn []
      [re-com/v-box
       :children
       [[re-com/label
         :label (str label ": " (target-value @value) )]
        [re-com/slider
         :style {:width "100%"}
         :model (target-value @value)
         :min min
         :max max
         :on-change #(dispatch [:update ks %])]]])))

;; Perhaps the definition of slider could be expanded to accomodate this
;; behavior, but whatever.
(defn size-spacing-ratio-slider [{:keys [label min max]}]
  (let [value (subscribe [:size-spacing-ratio])]
    (fn []
      [re-com/v-box
       :children
       [[re-com/label
         :label (str label ": " @value)]
        [re-com/slider
         :style {:width "100%"}
         :model (* 100 @value)
         :min min
         :max max
         :on-change #(dispatch [:update :size-spacing-ratio (/ % 100)])]]])))

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
         [:h3 "Geometry"]
         [slider [:columns] {:label "Columns" :min 2 :max 20}]
         [slider [:rows] {:label "Rows" :min 2 :max 20}]
         [slider [:width] {:label "Width" :min 100 :max 1000}]
         [slider [:height] {:label "Height" :min 100 :max 1000}]
         [size-spacing-ratio-slider {:label "Size/Spacing Ratio" :min 25 :max 75}]
         [:h3 "Lines"]
         [slider [:line-attributes :stroke-width] {:label "Thickness" :min 1 :max 100}]
         [:h3 "Rooms"]
         ; TODO
         [re-com/button
          :label "Generate Maze"
          :on-click #(dispatch [:generate-maze])]
         ]]
       :panel-2
       [:div {:style (merge rounded-panel
                            {:width "100%"
                             :margin-right "20px"})}
        [maze-svg]]
       ]]]))
