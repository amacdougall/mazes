(defproject mazes "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"],
                 [com.rpl/specter "0.12.0"]]
  :source-paths ["src/clj", "src/cljc"]
  :profiles
  {:dev
   {:dependencies [[org.clojure/test.check "0.9.0"]]
    :source-paths ["test/clj", "test/cljc"]
    :plugins [[com.jakemccrary/lein-test-refresh "0.10.0"]]
    :test-refresh {:notify-command ["lein-test-refresh-notify"]
                   :notify-on-success true
                   :quiet true}}}
  )
