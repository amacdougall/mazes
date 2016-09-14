(ns test.mazes.renderers.svg-test
  (:require [clojure.algo.generic.math-functions :refer [approx=]]
            [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [hiccup.core :as hiccup]
            [mazes.grid :as grid]
            [mazes.renderers.svg :refer :all :as svg]
            [test.mazes.helpers :refer [has-values? equal-numbers?]]
            [com.rpl.specter :as s]
            [com.rpl.specter.macros :as sm]))

;; NOTE: We're using a lot of this custom approximate-equality in these tests,
;; because we never know when something is going to return a double instead of
;; an int, and when division is involved, we find that this kind of check fails:
;; (== 2.0 1.9999999999999999999999) ...in defiance of our intent.
(defn- ≈ [a b]
  (clojure.algo.generic.math-functions/approx= a b 1e-2))

(stest/instrument)

(deftest test-root
  (testing "without render-environment options"
    (let [grid (grid/create-grid 2 2)
          render-env (render-environment grid)
          output (svg render-env)]
      (is (vector? output))
      (is (not (empty? output)))
      ; this simple Specter select could just be (first (filter map? output)), but
      ; selections will become more complex as the renderer SVG grows, so let's
      ; just use it consistently from the start.
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (≈ (:width default-svg-attributes) (:width attributes)))
        (is (≈ (:height default-svg-attributes) (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (equal-numbers?
              [0 0 (:width default-svg-attributes) (:height default-svg-attributes)]
              (map read-string (clojure.string/split (:viewbox attributes) #" ")))))))
  (testing "with render-environment options"
    (let [render-env (render-environment
                       (grid/create-grid 2 2)
                       {:width 1500
                        :height 1000
                        :viewbox {:x 20 :y 10 :width 500 :height 400}})
          output (svg render-env)]
      (let [attributes (sm/select-any [s/ALL map?] output)]
        (is (≈ 1500 (:width attributes)))
        (is (≈ 1000 (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (equal-numbers?
              [20 10 500 400]
              (map read-string (clojure.string/split (:viewbox attributes) #" "))))))))

(deftest test-rect
  (let [output (rect {:x 1 :y 2 :width 100 :height 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (≈ 1 (:x attributes)))
      (is (≈ 2 (:y attributes)))
      (is (≈ 100 (:width attributes)))
      (is (≈ 200 (:height attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))

(deftest test-line
  (let [output (line {:x1 10 :y1 20 :x2 100 :y2 200})]
    (is (vector? output))
    (is (not (empty? output)))
    (let [attributes (sm/select-any [s/ALL map?] output)]
      (is (≈ 10 (:x1 attributes)))
      (is (≈ 20 (:y1 attributes)))
      (is (≈ 100 (:x2 attributes)))
      (is (≈ 200 (:y2 attributes)))
      (is (= default-stroke-attributes (select-keys attributes (keys default-stroke-attributes)))))))

(deftest test-render-environment
  (testing "without explicit options"
    (let [columns 4
          rows 6
          {:keys [width height margin]} default-render-environment-options
          env (render-environment (grid/create-grid columns rows))]
      (is (not (nil? env)))
      (is (map? env))
      (is (≈ margin (:margin env)))
      (is (≈ width
             (+ (* (:cell-width env) columns)
                (* (:cell-h-spacing env) (- columns 1))
                (* 2 margin)))
          "Cell widths, separated by h-spacings, must equal width minus
          margin on each side.")
      (is (≈ height
             (+ (* (:cell-height env) rows)
                (* (:cell-v-spacing env) (- rows 1))
                (* 2 margin)))
          "Cell heights, separated by v-spacings, must equal width minus
          margin on each side.")))
  (testing "with explicit options"
    (let [columns 8
          rows 4
          width 800
          height 600
          margin 5
          size-spacing-ratio 0.6
          env (render-environment
                (grid/create-grid columns rows)
                {:width width
                 :height height
                 :margin margin
                 :size-spacing-ratio size-spacing-ratio
                 :rect-attributes {:stroke-width 10 :stroke "green" :fill "yellow"}
                 :line-attributes {:stroke-width 30 :stroke "blue"}})]
      (is (not (nil? env)))
      (is (map? env))
      (is (≈ width (:width (:viewbox env))))
      (is (≈ height (:height (:viewbox env))))
      (is (≈ margin (:margin env)))
      (is (≈ width
             (+ (* (:cell-width env) columns)
                (* (:cell-h-spacing env) (- columns 1))
                (* 2 margin)))
          "Cell widths, separated by h-spacings, must equal width minus
          margin on each side.")
      (is (≈ height
             (+ (* (:cell-height env) rows)
                (* (:cell-v-spacing env) (- rows 1))
                (* 2 margin)))
          "Cell heights, separated by v-spacings, must equal width minus
          margin on each side.")))
  (testing "with incomplete explicit options"
    (let [env (render-environment
                (grid/create-grid 2 2)
                {:rect-attributes {:stroke-width 10 :stroke "green" :fill "yellow"}
                 :line-attributes {:stroke-width 30 :stroke "blue"}})]
      (is (not (nil? env)))
      (is (map? env))
      (is (map? (:viewbox env)))
      (is (≈ (:width default-render-environment-options) (:width (:viewbox env))))
      (is (≈ (:height default-render-environment-options) (:height (:viewbox env)))))))

(deftest test-room-geometry
  (let [rows 8
        columns 4
        grid (grid/create-grid rows columns)
        render-env (render-environment grid)
        top-left-cell (grid/find-cell grid 0 0)
        top-left (room-geometry render-env top-left-cell)
        top-right-cell (grid/find-cell grid (- rows 1) 0)
        top-right (room-geometry render-env top-right-cell)
        bottom-left-cell (grid/find-cell grid 0 (- columns 1))
        bottom-left (room-geometry render-env bottom-left-cell)
        bottom-right-cell (grid/find-cell grid (- rows 1) (- columns 1))
        bottom-right (room-geometry render-env bottom-right-cell) ]
    (testing "return type"
      (is (map? top-left)))


    (testing "top left"
      (is (≈ (:margin render-env) (:x top-left)))
      (is (≈ (:margin render-env) (:y top-left)))
      (is (≈ (:cell-width render-env) (:width top-left)))
      (is (≈ (:cell-height render-env) (:height top-left))))

    (testing "top right"
      (is (≈ (+ (:margin render-env)
                 (* (::grid/x top-right-cell) (:cell-width render-env))
                 (* (::grid/x top-right-cell) (:cell-h-spacing render-env)))
              (:x top-right)))
      (is (≈ (:margin render-env) (:y top-right)))
      (is (≈ (:cell-width render-env) (:width top-right)))
      (is (≈ (:cell-height render-env) (:height top-right))))

    (testing "bottom left"
      (is (≈ (:margin render-env) (:x bottom-left)))
      (is (≈ (+ (:margin render-env)
                 (* (::grid/y bottom-left-cell) (:cell-height render-env))
                 (* (::grid/y bottom-left-cell) (:cell-v-spacing render-env)))
              (:y bottom-left)))
      (is (≈ (:cell-width render-env) (:width bottom-left)))
      (is (≈ (:cell-height render-env) (:height bottom-left))))

    (testing "bottom right"
      (is (≈ (+ (:margin render-env)
                 (* (::grid/x bottom-right-cell) (:cell-width render-env))
                 (* (::grid/x bottom-right-cell) (:cell-h-spacing render-env)))
              (:x bottom-right)))
      (is (≈ (+ (:margin render-env)
                 (* (::grid/y bottom-right-cell) (:cell-height render-env))
                 (* (::grid/y bottom-right-cell) (:cell-v-spacing render-env)))
              (:y bottom-right)))
      (is (≈ (:cell-width render-env) (:width bottom-right)))
      (is (≈ (:cell-height render-env) (:height bottom-right))))))

(deftest test-render-rect
  (let [grid (grid/create-grid 1 1)
        render-env (render-environment grid)
        rect (render-rect render-env (grid/find-cell grid 0 0))]
    (is (vector? rect))
    (is (= :rect (first rect)))
    (is (map? (last rect)))
    (is (has-values? (room-geometry render-env (grid/find-cell grid 0 0)) (last rect)))))

(deftest test-anchor-point
  (let [g {:x 10 :y 20 :width 100 :height 200}]
    (is (equal-numbers? [60 20] (anchor-point g ::grid/n)))
    (is (equal-numbers? [110 20] (anchor-point g ::grid/ne)))
    (is (equal-numbers? [110 120] (anchor-point g ::grid/e)))
    (is (equal-numbers? [110 220] (anchor-point g ::grid/se)))
    (is (equal-numbers? [60 220] (anchor-point g ::grid/s)))
    (is (equal-numbers? [10 220] (anchor-point g ::grid/sw)))
    (is (equal-numbers? [10 120] (anchor-point g ::grid/w)))
    (is (equal-numbers? [10 20] (anchor-point g ::grid/nw)))))

(deftest test-render-line
  (let [grid (grid/create-grid 2 2)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/e)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/s)
        render-env (render-environment grid)
        start-cell (grid/find-cell grid 0 0)
        start-room (room-geometry render-env start-cell)
        end-cell (grid/move grid start-cell ::grid/e)
        end-room (room-geometry render-env end-cell)
        line (render-line render-env start-cell ::grid/e)]
    (is (vector? line))
    (is (= :line (first line)))
    (is (map? (last line)))
    (is (has-values? default-stroke-attributes (last line)))
    (let [{:keys [x1 y1 x2 y2]} (last line)]
      (is (= (anchor-point start-room ::grid/e) [x1 y1]))
      (is (= (anchor-point end-room ::grid/w) [x2 y2])))))

(deftest test-render-cell
  (let [grid (grid/create-grid 2 2)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/e)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/s)
        grid (grid/link grid (grid/find-cell grid 1 0) ::grid/s)
        render-env (render-environment grid)
        start-cell (grid/find-cell grid 0 0)
        start-room (room-geometry render-env start-cell)
        end-cell (grid/move grid start-cell ::grid/e)
        end-room (room-geometry render-env end-cell)]
    (let [g (render-cell render-env start-cell)]
      (is (vector? g))
      (is (> (count g) 0))
      (is (= :g (first g)))
      (let [rect (find-rect g)
            lines (find-lines g)]
        (is (has-values? (room-geometry render-env start-cell) (last rect)))
        ; the two grid-links above should give this room two connections
        (is (= 2 (count lines)))))
    (let [g (render-cell render-env end-cell)]
      (let [rect (find-rect g)
            lines (find-lines g)]
        (is (has-values? (room-geometry render-env end-cell) (last rect)))
        ; this room should have a line only going south; previous room will have
        ; accounted for the east-west line
        (is (= 1 (count lines)))))))

(deftest test-render
  (let [grid (grid/create-grid 2 2)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/e)
        grid (grid/link grid (grid/find-cell grid 0 0) ::grid/s)]
    (testing "with default render environment"
      (let [render-env (render-environment grid)
            output (render grid render-env)]
        (is (vector? output))
        (is (= :svg (first output)))
        (is (= 4 (count (sm/select [s/ALL vector?] output)))
            "Four cell groups should be rendered.")
        (is (= 2 (count (sm/select
                          [s/ALL (is-svg-tag? :g) s/ALL (is-svg-tag? :line)]
                          output)))
            "Two connecting lines should be rendered.")
        (is (string? (hiccup/html output)) "Hiccup string output should succeed.")))
    (testing "with custom render environment"
      (let [render-env (render-environment grid {:line-attributes {:stroke-width 10}})
            output (render grid render-env)]
        (is (vector? output))
        (is (= :svg (first output)))
        (is (= 4 (count (sm/select [s/ALL vector?] output)))
            "Four cell groups should be rendered.")
        (let [lines (sm/select
                      [s/ALL (is-svg-tag? :g) s/ALL (is-svg-tag? :line)]
                      output)]
          (is (= 2 (count lines)) "Two connecting lines should be rendered.")
          (is (= 10 (:stroke-width (last (first lines))))
              "Connecting lines should use render-env line options."))
        (is (string? (hiccup/html output)) "Hiccup string output should succeed.")))))
