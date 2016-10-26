(ns mazes.renderers.text
  "Renderer which generates simple ASCII art for maze grids."
  (:require [mazes.grid :as g]))

(def body-east-open "    ")
(def body-east-wall "   |")
(def bottom-south-open "   +")
(def bottom-south-wall "---+")

(defn render-cell [cell]
  {:body (if (g/has-exit? cell ::g/e) body-east-open body-east-wall)
   :bottom (if (g/has-exit? cell ::g/s) bottom-south-open bottom-south-wall)})

(defn render-row [result row]
  (let [cell-results (map render-cell row)]
    (-> result
      (update :body conj (-> []
                           (conj "|")
                           (into (map :body cell-results))
                           (conj "\n")))
      (update :bottom conj (-> []
                             (conj "+")
                             (into (map :bottom cell-results))
                             (conj "\n"))))))

(defn render
  "Given a grid, returns a string representation of the maze as ASCII art. May
  not provide good results for large mazes; use common sense."
  [grid]
  (let [ir {:top [], :body [], :bottom []}
        column-count (count (first grid))
        top-border ["+"
                    (apply str (map (constantly "---+") (range column-count)))
                    "\n"]
        row-results (reduce render-row {:body [] :bottom []} grid)
        ir (assoc row-results :top top-border)]
    (apply str (concat (:top ir) (flatten (interleave (:body ir) (:bottom ir)))))))
