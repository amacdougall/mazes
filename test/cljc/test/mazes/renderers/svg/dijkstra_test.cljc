(ns test.mazes.renderers.svg.dijkstra-test
  (:require [clojure.algo.generic.math-functions :refer [approx=]]
            [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [hiccup.core :as hiccup]
            [mazes.grid :as g]
            [mazes.renderers.core :as r]
            [mazes.renderers.svg.core :refer :all]
            [test.mazes.helpers :refer [has-values? equal-numbers? ≈]]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

(stest/instrument)

;; NOTE: test calls mazes.renderers.core/render-cell, which is a multimethod.
;; The test specifically exercises the implementation provided by svg.dijkstra.
(deftest test-render-cell
  )
