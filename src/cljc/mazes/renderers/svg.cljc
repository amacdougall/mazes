(ns mazes.renderers.svg
  "Renderer which generates SVG markup for maze grids. The render function
  produces Hiccup data; use the render-svg function to get an SVG string."
  (:require [mazes.grid :as grid]
            [clojure.spec :as spec]))

(def default-svg-attributes
  {:version "1.1"
   :base-profile "full"
   :xmlns "http://www.w3.org/2000/svg"
   :width 400
   :height 300
   :viewbox {:x 0
             :y 0
             :width 400
             :height 300}})

(defn svg
  ([]
   (svg default-svg-attributes))
  ([{:keys [:width :height :viewbox] :as options}]
   (let [viewbox (or viewbox
                     (merge (:viewbox default-svg-attributes)
                            {:width width :height height}))
         viewbox-values (map viewbox [:x :y :width :height])
         viewbox-string (clojure.string/join " " viewbox-values)
         ; note that we are outputting NON-namespaced keys for Hiccup output
         svg-attributes (merge default-svg-attributes
                               {:width width
                                :height height
                                :viewbox viewbox-string})]
     [:svg svg-attributes])))

(defn render
  "Given a grid, returns an SVG rendering as Hiccup data structures. Accepts an
  options map with the following keys. All numeric values are in user units.

    :width -   The width of the SVG. Default 400.
    :height -  The height of the SVG. Default 300.
    :viewbox - An {:x, :y, :width, :height} map. Defaults to the width and
               height of the SVG."
  ; TODO: fully implement these functions; right now they just output the SVG base tag.
  ([grid]
   (svg))
  ([grid options]
   (svg options)))


