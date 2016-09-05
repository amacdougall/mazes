(ns test.mazes.renderers.svg-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [mazes.grid :as grid]
            [mazes.renderers.svg :refer :all :as svg]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]
            ))

;; NOTE: We're using a lot of == in these tests, because we never know when
;; something is going to return a double instead of an int.

(stest/instrument)

;; Helper: true if the two collections contain the same numbers in the same
;; order, regardless of the types of the collections or numbers. Uses ==.
(defn equal-numbers? [c1 c2]
  (every? #(apply == %) (partition 2 (interleave c1 c2))))

(deftest test-root
  (testing "without options hash"
    (let [output (svg)]
      (is (vector? output))
      (is (not (empty? output)))
      ; this simple Specter select could just be (first (filter map? output)), but
      ; selections will become more complex as the renderer SVG grows, so let's
      ; just use it consistently from the start.
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (== (:width default-svg-attributes) (:width attributes)))
        (is (== (:height default-svg-attributes) (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (equal-numbers?
              [0 0 (:width default-svg-attributes) (:height default-svg-attributes)]
              (map read-string (clojure.string/split (:viewbox attributes) #" ")))))))
  (testing "with options hash"
    (let [output (render (grid/create-grid 2 2)
                         {:width 1500
                          :height 1000
                          :viewbox {:x 20 :y 10 :width 500 :height 400}})]
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (== 1500 (:width attributes)))
        (is (== 1000 (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (equal-numbers?
              [20 10 500 400]
              (map read-string (clojure.string/split (:viewbox attributes) #" "))))))))

(deftest test-rect
  (let [output (rect {:x 1 :y 2 :width 100 :height 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (== 1 (:x attributes)))
      (is (== 2 (:y attributes)))
      (is (== 100 (:width attributes)))
      (is (== 200 (:height attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))

(deftest test-line
  (let [output (line {:x1 10 :y1 20 :x2 100 :y2 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (== 10 (:x1 attributes)))
      (is (== 20 (:y1 attributes)))
      (is (== 100 (:x2 attributes)))
      (is (== 200 (:y2 attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))

(deftest test-render-environment
  (testing "without explicit options"
    (let [columns 4
          rows 6
          {:keys [total-width total-height margin]} default-render-environment-options
          env (render-environment (grid/create-grid columns rows))]
      (is (not (nil? env)))
      (is (map? env))
      (is (== total-width
              (+ (* (:cell-width env) columns)
                 (* (:cell-h-spacing env) (- columns 1))
                 (* 2 margin)))
          "Cell widths, separated by h-spacings, must equal total-width minus
          margin on each side.")
      (is (== total-height
              (+ (* (:cell-height env) rows)
                 (* (:cell-v-spacing env) (- rows 1))
                 (* 2 margin)))
          "Cell heights, separated by v-spacings, must equal total-width minus
          margin on each side.")))
  (testing "with explicit options"
    (let [columns 8
          rows 4
          total-width 800
          total-height 600
          margin 5
          size-spacing-ratio 0.6
          env (render-environment (grid/create-grid columns rows)
                                  {:total-width total-width
                                   :total-height total-height
                                   :margin margin
                                   :size-spacing-ratio size-spacing-ratio})]
      (is (not (nil? env)))
      (is (map? env))
      (is (== total-width
              (+ (* (:cell-width env) columns)
                 (* (:cell-h-spacing env) (- columns 1))
                 (* 2 margin)))
          "Cell widths, separated by h-spacings, must equal total-width minus
          margin on each side.")
      (is (== total-height
              (+ (* (:cell-height env) rows)
                 (* (:cell-v-spacing env) (- rows 1))
                 (* 2 margin)))
          "Cell heights, separated by v-spacings, must equal total-width minus
          margin on each side."))))
