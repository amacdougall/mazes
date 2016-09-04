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
      (let [attributes (sm/select-any [s/ALL map?] output)
            defaults svg/default-svg-attributes]
        (is (= (:width defaults) (:width attributes)))
        (is (= (:height defaults) (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (= [0 0 (:width defaults) (:height defaults)]
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
               (map read-string (clojure.string/split (:viewbox attributes) #" "))))
        )
      )
    ))
