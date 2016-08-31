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
    (let [svg (render (grid/create-grid 2 2))]
      (is (vector? svg))
      (is (not (empty? svg)))
      ; this simple Specter select could just be (first (filter map? svg)), but
      ; selections will become more complex as the renderer SVG grows, so let's
      ; just use it consistently from the start.
      (let [attributes (sm/select-any [s/ALL map?] svg)]
        (is (= (::svg/width svg/defaults) (:width attributes)))
        (is (= (::svg/height svg/defaults) (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (= [0 0 (svg/defaults ::svg/width) (svg/defaults ::svg/height)]
               (map read-string (clojure.string/split (:viewbox attributes) #" ")))))))
  (testing "with options hash"
    (let [svg (render (grid/create-grid 2 2)
                      {::svg/width 1500
                       ::svg/height 1000
                       ::svg/viewbox {::svg/x 20
                                      ::svg/y 10
                                      ::svg/width 500
                                      ::svg/height 400}})]
      (let [attributes (sm/select-any [s/ALL map?] svg)]
        (is (= 1500 (:width attributes)))
        (is (= 1000 (:height attributes)))
        (is (string? (:viewbox attributes)))
        (is (= [20 10 500 400]
               (map read-string (clojure.string/split (:viewbox attributes) #" "))))
        )
      )
    ))
