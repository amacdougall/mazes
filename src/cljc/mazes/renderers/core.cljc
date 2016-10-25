(ns mazes.renderers.core)

(defmulti render-cell (fn [env _] (:type (:annotations env))))
