(ns test.mazes.renderers.svg.dijkstra-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [mazes.algorithms.dijkstra :as d]
            [mazes.grid :as g]
            [mazes.renderers.core :as r]
            [mazes.renderers.svg.core :as svg]
            [mazes.renderers.svg.dijkstra :as d-svg]
            [test.mazes.helpers :refer [has-values? equal-numbers? ≈]]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

(stest/instrument)

;; NOTE: test calls mazes.renderers.core/render-cell, which is a multimethod.
;; The test specifically exercises the implementation provided by svg.dijkstra.
(deftest test-render-cell
  (let [grid (g/create-grid 2 2)
        grid (g/link-path grid (g/find-cell grid 0 0) [::g/e ::g/s ::g/w])
        origin (g/find-cell grid 0 0)
        destination (g/find-cell grid 0 1)
        solution (d/solve grid origin destination)
        annotations {:annotations (merge solution {:type :dijkstra})}
        render-env (merge (svg/render-environment grid) annotations)]
    (let [{:keys [rect text]} (r/render-cell render-env origin)]
      (is (not (nil? text)))
      (is (= (get (::d/distances solution) origin) (last text)))
      (is (= (-> d-svg/path-highlight :rect-attributes :fill)
             (-> rect svg/attributes :fill))
          "cells on the solution path should have the path highlight fill")
      (is (= (-> d-svg/path-highlight :rect-attributes :stroke)
             (-> rect svg/attributes :stroke))
          "cells on the solution path should have the path highlight stroke"))
    (let [{:keys [rect text]} (r/render-cell render-env destination)]
      (is (not (nil? text)))
      (is (= (get (::d/distances solution) destination) (last text)))
      (is (= (-> d-svg/path-highlight :rect-attributes :fill)
             (-> rect svg/attributes :fill))
          "cells on the solution path should have the path highlight fill")
      (is (= (-> d-svg/path-highlight :rect-attributes :stroke)
             (-> rect svg/attributes :stroke))
          "cells on the solution path should have the path highlight stroke"))))
