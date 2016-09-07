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

(deftest test-render-cell
  (let [rows 8
        columns 4
        grid (grid/create-grid rows columns)
        env (render-environment grid)
        top-left-cell (grid/find-cell grid 0 0)
        top-left (render-cell env top-left-cell)
        top-right-cell (grid/find-cell grid (- rows 1) 0)
        top-right (render-cell env top-right-cell)
        bottom-left-cell (grid/find-cell grid 0 (- columns 1))
        bottom-left (render-cell env bottom-left-cell)
        bottom-right-cell (grid/find-cell grid (- rows 1) (- columns 1))
        bottom-right (render-cell env bottom-right-cell)
        ; given a tag name symbol, such as :rect, return a Specter path to find
        ; it in a render-cell result
        tag-path (fn [tag] [s/ALL vector? #(= tag (first %)) s/LAST])
        ; rect-attrs and line-attrs take a render-cell result: a <g> tag
        rect-attrs (fn [g] (sm/select-any (tag-path :rect) g))
        line-attrs (fn [g] (sm/select-any (tag-path :line) g))]
    (is (vector? top-left))
    (is (= :g (first top-left)))
    (is (some (partial = :rect)
              (sm/select [s/ALL vector? s/FIRST] top-left)))
    (let [attrs (rect-attrs top-left)]
      (is (== 0 (:x attrs)))
      (is (== 0 (:y attrs)))
      (is (== (:cell-width env) (:width attrs)))
      (is (== (:cell-height env) (:height attrs))))
    (let [attrs (rect-attrs top-right)]
      (is (== (+ (* (::grid/x top-right-cell) (:cell-width env))
                 (* (::grid/x top-right-cell) (:cell-h-spacing env)))
              (:x attrs)))
      (is (== 0 (:y attrs)))
      (is (== (:cell-width env) (:width attrs)))
      (is (== (:cell-height env) (:height attrs))))
    (let [attrs (rect-attrs bottom-left)]
      (is (== 0 (:x attrs)))
      (is (== (+ (* (::grid/y bottom-left-cell) (:cell-height env))
                 (* (::grid/y bottom-left-cell) (:cell-v-spacing env)))
              (:y attrs)))
      (is (== (:cell-width env) (:width attrs)))
      (is (== (:cell-height env) (:height attrs))))
    (let [attrs (rect-attrs bottom-right)]
      (is (== (+ (* (::grid/x bottom-right-cell) (:cell-width env))
                 (* (::grid/x bottom-right-cell) (:cell-h-spacing env)))
              (:x attrs)))
      (is (== (+ (* (::grid/y bottom-right-cell) (:cell-height env))
                 (* (::grid/y bottom-right-cell) (:cell-v-spacing env)))
              (:y attrs)))
      (is (== (:cell-width env) (:width attrs)))
      (is (== (:cell-height env) (:height attrs))))))
