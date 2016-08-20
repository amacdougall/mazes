(ns mazes.core-test
  (:require [clojure.test :refer :all]
            [mazes.core :refer :all :as mazes]))

(deftest sanity
  (is (= true true)))

(deftest test-create-cell
  (let [c (create-cell 0 0)]
    (is (= 0 (::mazes/x c)))
    (is (= 0 (::mazes/y c)))))

(deftest test-create-grid
  (let [grid (create-grid 4 2)]
    (is (= 2 (count grid)) "grid had incorrect row count")
    (is (= 4 (count (first grid))) "grid had incorrect column count")))

(deftest test-grid-contains
  (let [grid (create-grid 2 2)]
    (is (not (grid-contains? grid -1 -1))
        "should be false because coordinates were negative")
    (is (not (grid-contains? grid 100 100))
        "should be false because coordinates were extreme")
    (is (grid-contains? grid 0 0)
        "should be true because coordinates at top left of map")
    (is (grid-contains? grid 1 1)
        "should be true with coordinates at bottom right of map")))

(deftest test-find-cell
  (let [grid (create-grid 1 2)]
    (is (= (create-cell 0 0) (find-cell grid 0 0)))
    (is (= (create-cell 0 1) (find-cell grid 0 1)))
    (is (nil? (find-cell grid 0 2)))))

(deftest test-random-cell
  (let [grid (create-grid 2 2)
        random-cell (random-cell grid)
        {rx ::mazes/x ry ::mazes/y} random-cell]
    (is (not (nil? random-cell)))
    (is (grid-contains? grid rx ry))
    (is (= random-cell (find-cell grid rx ry)))))

(deftest test-link
  (let [grid (create-grid 2 1)
        ; link the top left cell to its eastern neighbor
        grid (link grid (find-cell grid 0 0) ::mazes/e)
        cell-a (find-cell grid 0 0)
        cell-b (find-cell grid 1 0)]
    (is (contains? (::mazes/exits cell-a) ::mazes/e))
    (is (contains? (::mazes/exits cell-b) ::mazes/w))
    (is (= 1 (count (::mazes/exits cell-a))))
    (is (= 1 (count (::mazes/exits cell-b))))))
