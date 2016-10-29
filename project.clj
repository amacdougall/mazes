(defproject mazes "0.1.0-SNAPSHOT"
  :description "Playground for maze generation algorithms and visualizations."
  :url "http://example.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"],
                 [com.rpl/specter "0.12.0"]
                 [hiccup "1.0.5"]
                 ; clojurescript
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0-rc"]
                 [re-frame "0.8.0"]
                 [re-com "0.8.3"]]
  :source-paths ["src/clj", "src/cljc", "src/cljs"]
  :plugins [[lein-cljsbuild "1.1.4"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :figwheel {:css-dirs ["resources/public/css"]}
  :profiles
  {:dev
   {:dependencies [[org.clojure/test.check "0.9.0"]
                   [org.clojure/algo.generic "0.1.2"]
                   [binaryage/devtools "0.6.1"]]
    :source-paths ["test/clj", "test/cljc"]
    :plugins [[com.jakemccrary/lein-test-refresh "0.10.0"]
              [lein-figwheel "0.5.4-3"]]
    :test-refresh {:notify-command ["lein-test-refresh-notify"]
                   :notify-on-success true
                   :quiet true}
    :test-selectors {:default (complement :slow)
                     :slow :slow
                     :all (constantly true)}}}
  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljc", "src/cljs"]
     :figwheel     {:on-jsload "mazes.web.core/mount-root"}
     :compiler     {:main                 mazes.web.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljc", "src/cljs"]
     :compiler     {:main            mazes.web.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}
    ]}
  )
