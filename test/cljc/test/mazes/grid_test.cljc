(ns test.mazes.grid-test
  (:require [clojure.test :refer :all]
            [clojure.spec.test :as stest]
            [mazes.grid :refer :all :as grid]))

(stest/instrument)

(deftest test-create-cell
  (let [c (grid/create-cell 0 0)]
    (is (= 0 (::grid/x c)))
    (is (= 0 (::grid/y c)))))

(deftest test-create-grid
  (let [grid (grid/create-grid 4 2)]
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
        {rx ::grid/x ry ::grid/y} random-cell]
    (is (not (nil? random-cell)))
    (is (grid-contains? grid rx ry))
    (is (= random-cell (find-cell grid rx ry)))))

(deftest test-next-cell
  (let [grid (create-grid 2 2)
        first-cell (find-cell grid 0 0)
        row-end-cell (find-cell grid 1 0)
        grid-end-cell (find-cell grid 1 1)]
    (let [cell (next-cell grid first-cell)]
      (is (not (nil? cell)))
      (is (= [1 0] ((juxt ::grid/x ::grid/y) cell))))
    (let [cell (next-cell grid row-end-cell)]
      (is (not (nil? cell)))
      (is (= [0 1] ((juxt ::grid/x ::grid/y) cell))))
    (let [cell (next-cell grid grid-end-cell)]
      (is (nil? cell)))))

(deftest test-link
  (let [grid (create-grid 2 1)
        ; link the top left cell to its eastern neighbor
        grid (link grid (find-cell grid 0 0) ::grid/e)
        cell-a (find-cell grid 0 0)
        cell-b (find-cell grid 1 0)]
    (is (contains? (::grid/exits cell-a) ::grid/e))
    (is (contains? (::grid/exits cell-b) ::grid/w))
    (is (= 1 (count (::grid/exits cell-a))))
    (is (= 1 (count (::grid/exits cell-b))))))

(deftest test-link-path
  (let [grid (create-grid 3 3)
        ; make a path [0 0], [1 0], [1 1], [1 0]
        grid (link-path grid (find-cell grid 0 0)
                        [::grid/e ::grid/s ::grid/w])]
    (is (contains? (::grid/exits (find-cell grid 0 0)) ::grid/e))
    (is (contains? (::grid/exits (find-cell grid 1 0)) ::grid/s))
    (is (contains? (::grid/exits (find-cell grid 1 1)) ::grid/w))))

(deftest test-cells-on-path
  (let [grid (create-grid 2 2)
        path [::grid/e ::grid/s ::grid/w]
        result (cells-on-path grid (find-cell grid 0 0) path)]
    (is (= [(find-cell grid 0 0)
            (find-cell grid 1 0)
            (find-cell grid 1 1)]
           result))))

(deftest test-linked-cells
  (let [grid (create-grid 3 3)
        ; link the top left cell to its eastern neighbor
        grid (link grid (find-cell grid 0 0) ::grid/e)
        ; link the center cell in the cardinal directions
        grid (link grid (find-cell grid 1 1) ::grid/n)
        grid (link grid (find-cell grid 1 1) ::grid/e)
        grid (link grid (find-cell grid 1 1) ::grid/s)
        grid (link grid (find-cell grid 1 1) ::grid/w)]
    (let [cell (find-cell grid 0 0)
          neighbors (linked-cells grid cell)]
      (is (= 1 (count neighbors)))
      (is (= (find-cell grid 1 0) (first neighbors))))
    (let [cell (find-cell grid 1 1)
          directions [::grid/n ::grid/e ::grid/s ::grid/w]
          expected-neighbors (set (map (partial move grid cell) directions))
          neighbors (linked-cells grid cell)]
      (is (= 4 (count neighbors)))
      (is (every? expected-neighbors neighbors)))))

(deftest test-has-exit?
  (let [grid (create-grid 3 3)
        grid (link grid (find-cell grid 1 1) ::grid/e)
        grid (link grid (find-cell grid 1 1) ::grid/s)
        cell (find-cell grid 1 1)
        east (move grid cell ::grid/e)
        south (move grid cell ::grid/s)
        north (move grid cell ::grid/n)
        neighbors (linked-cells grid cell)]
    (is (not (empty? neighbors)))
    (is (= 2 (count neighbors)))
    (is (every? #{east south} neighbors))))


(deftest test-column-count
  (let [grid (create-grid 20 20)]
    (is (= 20 (column-count grid)))))

(deftest test-row-count
  (let [grid (create-grid 20 20)]
    (is (= 20 (row-count grid)))))
