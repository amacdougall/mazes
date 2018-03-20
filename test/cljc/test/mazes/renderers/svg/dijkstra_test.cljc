(ns test.mazes.renderers.svg.dijkstra-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test.alpha :as stest]
            [mazes.pathfinders.dijkstra :as d]
            [mazes.grid :as g]
            [mazes.renderers.core :as r]
            [mazes.renderers.svg.core :as svg]
            [mazes.renderers.svg.dijkstra :as d-svg]
            [test.mazes.helpers :refer [has-values? equal-numbers? ≈]]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

(stest/instrument)

(def path-highlight #'mazes.renderers.svg.dijkstra/path-highlight)
(def current-highlight #'mazes.renderers.svg.dijkstra/current-highlight)
(def unvisited-highlight #'mazes.renderers.svg.dijkstra/unvisited-highlight)
(def distance-highlight #'mazes.renderers.svg.dijkstra/distance-highlight)

;; NOTE: test calls mazes.renderers.core/render-cell, which is a multimethod.
;; The test specifically exercises the implementation provided by svg.dijkstra.
(deftest test-render-cell
  (let [grid (g/create-grid 3 3)
        grid (g/link-path grid (g/find-cell grid [0 0]) [::g/e ::g/s ::g/w])
        origin (g/find-cell grid [0 0])
        destination (g/find-cell grid [0 1])
        intermediate (g/find-cell grid [1 0])
        unreachable (g/find-cell grid [2 0])
        solution (d/solve grid origin destination)
        annotations {:annotations (merge solution {:type :dijkstra})}
        render-env (merge (svg/render-environment grid) annotations)]
    (testing "origin cell"
      (let [{:keys [rect text]} (r/render-cell render-env origin)]
        (is (not (nil? text)))
        (is (= (get (::d/distances solution) origin) (last text)))
        (is (= (-> (path-highlight) :rect-attributes :fill)
               (-> rect svg/attributes :fill))
            "cell should have the path highlight fill")
        (is (= (-> (path-highlight) :rect-attributes :stroke)
               (-> rect svg/attributes :stroke))
            "cell should have the path highlight stroke")))
    (testing "destination cell"
      (let [{:keys [rect text]} (r/render-cell render-env destination)]
        (is (not (nil? text)))
        (is (= (get (::d/distances solution) destination) (last text)))
        (is (= (-> (path-highlight) :rect-attributes :fill)
               (-> rect svg/attributes :fill))
            "cell should have the path highlight fill")
        (is (= (-> (path-highlight) :rect-attributes :stroke)
               (-> rect svg/attributes :stroke))
            "cell should have the path highlight stroke")))
    (testing "intermediate cell on solution path"
      (let [{:keys [rect text]} (r/render-cell render-env intermediate)]
        (is (not (nil? text)))
        (is (= (get (::d/distances solution) intermediate) (last text)))
        (is (= (-> (path-highlight) :rect-attributes :fill)
               (-> rect svg/attributes :fill))
            "")
        (is (= (-> (path-highlight) :rect-attributes :stroke)
               (-> rect svg/attributes :stroke))
            "cells on the solution path should have the path highlight stroke")))
    (testing "unreachable cell"
      (let [{:keys [rect text]} (r/render-cell render-env unreachable)]
        (is (not (nil? text)))
        (is (= d-svg/infinite-distance-text (last text))
            "unvisited cells should have the infinite-distance text")))))
