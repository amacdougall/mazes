(ns mazes.web.views
    (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
              [re-com.core :as re-com]
              [mazes.algorithms.sidewinder :as sidewinder]
              [mazes.grid :as g]
              [mazes.renderers.svg.core :as svg]
              [mazes.renderers.svg.dijkstra]
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
(defn slider [ks {:keys [label min max step]}]
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
         :step (or step 1)
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

;; Given a keypath such as [:top] or [:top :node :leaf], returns an input field
;; which accepts only a color hex, which alters this value in the app db.
(defn color [ks {:keys [label]}]
  (let [value (subscribe [(first ks)])
        target-value (fn [top] (get-in top (rest ks)))]
    (fn []
      [re-com/v-box
       :children
       [[re-com/label :label label]
        [re-com/input-text
         :model (target-value @value)
         :on-change #(dispatch [:update ks %])
         :width "200px"
         ]]])))

(defn maze-controls []
  [re-com/v-box
   :gap "2.0rem"
   :children
   [[:h3 "Maze"]
    [slider [:columns] {:label "Columns" :min 2 :max 20}]
    [slider [:rows] {:label "Rows" :min 2 :max 20}]
    [re-com/button
     :label "Generate Maze"
     :on-click #(dispatch [:generate-maze])]]])

(defn layout-controls []
  [re-com/v-box
   :gap "2.0rem"
   :children
   [[:h3 "Geometry"]
    [slider [:width] {:label "Width" :min 100 :max 1000 :step 10}]
    [slider [:height] {:label "Height" :min 100 :max 1000 :step 10}]
    [size-spacing-ratio-slider {:label "Size/Spacing Ratio" :min 25 :max 75}]
    [:h3 "Lines"]
    [slider [:line-attributes :stroke-width] {:label "Thickness" :min 1 :max 100}]
    [color [:line-attributes :stroke] {:label "Color"}]
    [:h3 "Rooms"]
    [slider [:rect-attributes :stroke-width] {:label "Stroke Thickness" :min 1 :max 100}]
    [color [:rect-attributes :stroke] {:label "Stroke Color"}]
    [color [:rect-attributes :fill] {:label "Fill Color"}]]]
  )

(defn solution-controls []
  [re-com/v-box
   :gap "2.0rem"
   :children
   [[:h3 "Solution"]
    [re-com/button
     :label "Step"
     :on-click #(dispatch [:step-solution])]
    [re-com/button
     :label "Solve"
     :on-click #(dispatch [:solve-maze])]
    [re-com/button
     :label "Reset"
     :on-click #(dispatch [:reset-solution])]]])

(defn controls []
  (let [selected-tab (subscribe [:selected-controls-tab])
        tab-definitions [{:id :maze :label "Maze"}
                         {:id :layout :label "Layout"}
                         {:id :solution :label "Solution"}]
        change-tab #(dispatch [:update [:selected-controls-tab] %])]
    (fn []
      [re-com/v-box
       :gap "2.0rem"
       :children
       [[re-com/horizontal-tabs
         :tabs tab-definitions
         :model selected-tab
         :on-change change-tab]
        [(condp = @selected-tab
           :maze maze-controls
           :layout layout-controls
           :solution solution-controls)]]])))

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
       :initial-split "25%"
       :panel-1
       [re-com/v-box
        :style (merge rounded-panel
                      {:width "100%"
                       :overflow "auto"})
        :gap "2rem"
        :children
        [[controls]]]
       :panel-2
       [:div {:style (merge rounded-panel
                            {:width "100%"
                             :margin-right "20px"})}
        [maze-svg]]]]]))
