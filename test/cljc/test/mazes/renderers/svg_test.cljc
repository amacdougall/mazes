(ns test.mazes.renderers.svg-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [mazes.grid :as grid]
            [mazes.renderers.svg :refer :all :as svg]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]
            ))

(stest/instrument)

(deftest test-root
  (testing "without options hash"
    (let [output (svg)]
      (is (vector? output))
      (is (not (empty? output)))
      ; this simple Specter select could just be (first (filter map? output)), but
      ; selections will become more complex as the renderer SVG grows, so let's
      ; just use it consistently from the start.
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (= (:width default-svg-attributes) (:width attributes)))
        (is (= (:height default-svg-attributes) (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (= [0 0 (:width default-svg-attributes) (:height default-svg-attributes)]
               (map read-string (clojure.string/split (:viewbox attributes) #" ")))))))
  (testing "with options hash"
    (let [output (render (grid/create-grid 2 2)
                         {:width 1500
                          :height 1000
                          :viewbox {:x 20 :y 10 :width 500 :height 400}})]
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (= 1500 (:width attributes)))
        (is (= 1000 (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (= [20 10 500 400]
               (map read-string (clojure.string/split (:viewbox attributes) #" "))))))))

(deftest test-rect
  (let [output (rect {:x 1 :y 2 :width 100 :height 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (= 1 (:x attributes)))
      (is (= 2 (:y attributes)))
      (is (= 100 (:width attributes)))
      (is (= 200 (:height attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))

(deftest test-line
  (let [output (line {:x1 10 :y1 20 :x2 100 :y2 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (= 10 (:x1 attributes)))
      (is (= 20 (:y1 attributes)))
      (is (= 100 (:x2 attributes)))
      (is (= 200 (:y2 attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))
