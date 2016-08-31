(ns mazes.renderers.svg
  "Renderer which generates SVG markup for maze grids. The render function
  produces Hiccup data; use the render-svg function to get an SVG string."
  (:require [mazes.grid :as grid]
            [clojure.spec :as spec]))

(spec/def ::x int?)
(spec/def ::y int?)
(spec/def ::width int?)
(spec/def ::height int?)
(spec/def ::viewbox (spec/keys :req [::x ::y ::width ::height]))

(def defaults {::width 400
               ::height 300})

(defn render
  "Given a grid, returns an SVG rendering as Hiccup data structures. Accepts an
  options map with the following keys. All numeric values are in user units.

    ::width - The width of the SVG. Default 400.
    ::height - The height of the SVG. Default 300.
    ::viewbox - An {::x, ::y, ::width, ::height} map. Defaults to the width and
                height of the SVG."
  ([grid]
   (let [{:keys [::width ::height]} defaults]
     (render grid {::width width
                   ::height height
                   ::viewbox {::x 0, ::y 0, ::width width, ::height height}})))
  ([grid {:keys [::width ::height ::viewbox]}]
   [:svg {:width width
          :height height
          :viewbox (clojure.string/join " " (map viewbox [::x ::y ::width ::height]))}]))


