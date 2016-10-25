(ns mazes.renderers.svg.core
  "Renderer which generates SVG markup for maze grids. The render function
  produces Hiccup data; use the render-svg function to get an SVG string."
  (:require [mazes.grid :as g]
            [mazes.helpers :refer [deep-merge]]
            [mazes.renderers.core :as r]
            [clojure.spec :as spec]
            [com.rpl.specter :as s]
            #?(:clj [com.rpl.specter.macros :as sm :include-macros true]))
  #?(:cljs (:require-macros [com.rpl.specter.macros :as sm])))

(def default-svg-attributes
  {:version "1.1"
   :base-profile "full"
   :xmlns "http://www.w3.org/2000/svg"
   :width 1000
   :height 800
   :viewbox "0 0 1000 800"})

(def default-stroke-attributes
  {:stroke "black"
   :stroke-width 20
   :stroke-linejoin "miter"
   :stroke-linecap "square"})

(def default-rect-attributes
  (merge default-stroke-attributes
         {:fill "black"}))

; In principle, may differ from stroke attributes.
(def default-line-attributes default-stroke-attributes)

(def default-render-environment-options
  {:width (:width default-svg-attributes)
   :height (:height default-svg-attributes)
   :margin 20
   :size-spacing-ratio 0.5
   :rect-attributes default-rect-attributes
   :line-attributes default-line-attributes})

(defn rect [attributes]
  [:rect (merge default-rect-attributes attributes)])

(defn line [attributes]
  [:line (merge default-stroke-attributes attributes)])

(defn svg
  "Given a render environment generated by render-environment, returns an SVG
  base tag."
  [render-env]
  (let [{:keys [width height viewbox]} render-env
        viewbox-values (map viewbox [:x :y :width :height])
        viewbox-string (clojure.string/join " " viewbox-values)
        svg-attributes (merge default-svg-attributes
                              {:width width
                               :height height
                               :viewbox viewbox-string})]
    [:svg svg-attributes]))

(defn render-environment
  "Given a grid and an options map, returns a map of values to be used in
  rendering. All optional keys default to the corresponding value in
  default-render-environment-options.

  Options accepted:
    :width - Optional. The total width of the render area. Equal to the
      width property of the SVG tag.
    :height - Optional. The total height of the render area. Equal to the
      height property of the SVG tag.
    :size-spacing-ratio - Optional. A value between 0 and 1, defining the ratio
      between the size of cell and the space between them.
    :margin - Optional. The blank space around the edges of the rendered area.
      A higher margin will leave less space for cells.
    :viewbox - Optional. A map with :x, :y, :width, and :height keys. Used to
      generate the SVG viewbox attribute string.
    :rect-attributes - Optional. A map of SVG attributes to be applied to all rects.
    :line-attributes - Optional. A map of SVG attributes to be applied to all lines.

  Values returned:
    :grid - The grid which is being rendered; rendering functions may use this
      for additional information. For instance, drawing connections between
      rooms is simplified when g/find-cell is available.

    :width - Passed through from input options.
    :height - Passed through from input options.
    :viewbox - Passed through from input options.
    :margin - Passed through from input options.
    :rect-attributes - Passed through from input options.
    :line-attributes - Passed through from input options.

    :cell-width - The width of each cell to be rendered.
    :cell-height - The height of each cell to be rendered.
    :cell-h-spacing - The horizontal space between each cell.
    :cell-v-spacing - The vertical space between each cell."
  ([grid]
   (render-environment grid default-render-environment-options))
  ([grid options]
   ; use default-render-environment-options as base; add user options; add
   ; default viewbox; note the use of deep-merge, since some option values can
   ; be nested maps.
   (let [options (deep-merge default-render-environment-options options)
         options (deep-merge options
                             (when (and (:width options)
                                        (:height options)
                                        (nil? (:viewbox options)))
                               {:viewbox (assoc (select-keys options #{:width :height}) :x 0 :y 0)}))
         {:keys [width height size-spacing-ratio margin viewbox]} options
         columns (g/column-count grid)
         rows (g/row-count grid)
         cell-area-width (- width (* 2 margin))
         cell-area-height (- height (* 2 margin))
         width-per-cell (/ cell-area-width columns)
         height-per-cell (/ cell-area-height rows)
         cell-width (* width-per-cell size-spacing-ratio)
         cell-height (* height-per-cell size-spacing-ratio)]
     (merge options
            {:grid grid
             :cell-width cell-width
             :cell-height cell-height
             ; spacing exists between cells, but not on the far side of the last
             ; column/row; so divide the non-cell space by n - 1 (minimum of 1), not n.
             :cell-h-spacing (/ (- cell-area-width (* cell-width columns))
                                (max (- columns 1) 1))
             :cell-v-spacing (/ (- cell-area-height (* cell-height rows))
                                (max (- rows 1) 1))}))))

(defn room-geometry
  "Given a render-environment map and a ::g/cell, returns a map with keys :x,
  :y, :width, and :height, which can be used as attributes of an SVG rect."
  [{:keys [margin cell-width cell-height cell-h-spacing cell-v-spacing]} cell]
  {:x (+ margin (* (::g/x cell) (+ cell-width cell-h-spacing)))
   :y (+ margin (* (::g/y cell) (+ cell-height cell-v-spacing)))
   :width cell-width
   :height cell-height})

(defn anchor-point
  "Given a room-geometry result and the ::g/direction keyword for one of its
  corners, returns an [x y] coordinate pair vector for that point on the
  rendered cell. For instance, (anchor-point geometry ::g/nw) returns the
  coordinates of the top-left corner of the rect."
  [{:keys [x y width height]} direction]
  (let [left x
        h-center (+ x (/ width 2))
        right (+ x width)
        top y
        v-center (+ y (/ height 2))
        bottom (+ y height)]
    (condp = direction
      ::g/n  [h-center top]
      ::g/ne [right top]
      ::g/e  [right v-center]
      ::g/se [right bottom]
      ::g/s  [h-center bottom]
      ::g/sw [left bottom]
      ::g/w  [left v-center]
      ::g/nw [left top])))
(spec/fdef anchor-point
  :args (spec/cat :geometry map? :grid ::g/direction)
  :ret (spec/coll-of number? :min-count 2 :max-count 2))

(defn render-rect [render-env cell]
  (rect (merge (:rect-attributes render-env) (room-geometry render-env cell))))

(defn render-line [render-env cell direction]
  (let [start-room (room-geometry render-env cell)
        end-cell (g/move (:grid render-env) cell direction)
        end-room (room-geometry render-env end-cell)
        [x1 y1] (anchor-point start-room direction)
        [x2 y2] (anchor-point end-room (direction g/converse-directions))]
    (line (merge (:line-attributes render-env)
                 {:x1 x1, :y1 y1, :x2 x2, :y2 y2}))))
(spec/fdef render-line
  :args (spec/cat :render-env map? :cell ::g/cell :direction ::g/direction)
  :ret vector?)

(defn render-cell
  [render-env cell]
  (into [:g (render-rect render-env cell)]
        (map (partial render-line render-env cell)
             (filter #{::g/ne ::g/e ::g/se ::g/s} (::g/exits cell)))))
; TODO: spec multimethod?
(spec/fdef render-cell
  :args (spec/cat :render-env map? :cell ::g/cell :existing-lines (spec/? set?)))

(defmethod r/render-cell nil
  [render-env cell]
  (render-cell render-env cell))

; Specter helper functions for reading render-cell results
(defn is-svg-tag? [tag]
  (fn [coll]
    (and (vector? coll) (= tag (first coll)))))

(defn find-rect [g] (sm/select-any [s/ALL (is-svg-tag? :rect)] g))
(defn find-lines [g] (sm/select [s/ALL (is-svg-tag? :line)] g))
(defn find-text [g] (sm/select-any [s/ALL (is-svg-tag? :text)] g))

(defn attributes
  "Given any SVG element, returns its attributes map."
  [e] (sm/select-any [s/ALL map?] e))

(defn render
  "Given a render environment and a grid, returns an SVG rendering as Hiccup
  data structures. All numeric values are in user units."
  [render-env grid]
  (into (svg render-env)
        (map (partial r/render-cell render-env)
             (g/all-cells grid))))
