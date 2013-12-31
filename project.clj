(defproject wefeedus-client "0.0.1-SNAPSHOT"
  :description "A map based social appication to connect eaters and cooks."
  :url "http://github.com/ghubber/wefeedus-client/"
  :license {:name "AGPL v3"
            :url "https://www.gnu.org/licenses/agpl-3.0.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript #_"0.0-1835" "0.0-2127"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [domina "1.0.1"]
                 [ch.qos.logback/logback-classic "1.0.13" :exclusions [org.slf4j/slf4j-api]]
                 [io.pedestal/pedestal.app "0.2.2"]
                 [io.pedestal/pedestal.app-tools "0.2.2"]
                 [com.cemerick/piggieback "0.1.0"]]
  :min-lein-version "2.0.0"
  :source-paths ["app/src" "app/templates"]
  :resource-paths ["config"]
  :target-path "out/"
  :repl-options  {:init-ns user
                  :init (try
                          (use 'io.pedestal.app-tools.dev)
                          (catch Throwable t
                            (println "ERROR: There was a problem loading io.pedestal.app-tools.dev")
                            (clojure.stacktrace/print-stack-trace t)
                            (println)))
                  :welcome (println "Welcome to pedestal-app! Run (tools-help) to see a list of useful functions.")
                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :main ^{:skip-aot true} io.pedestal.app-tools.dev)
