(ns test.mazes.renderers.svg.dijkstra-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [mazes.algorithms.dijkstra :as d]
            [mazes.grid :as g]
            [mazes.renderers.core :as r]
            [mazes.renderers.svg.core :as svg]
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
    ;; first, duplicate the svg.core_test version to make sure we didn't break anything
    ;; now test the distance text
    (let [g (r/render-cell render-env origin)
          text (svg/find-text g)]
      (is (not (nil? text)))
      (is (= (get (::d/distances solution) origin) (last text))))
    (let [g (r/render-cell render-env destination)
          text (svg/find-text g)]
      (is (not (nil? text)))
      (is (= (get (::d/distances solution) destination) (last text))))))
