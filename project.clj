(defproject cljg "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [
                 ;; ClojureScript
                 [org.clojure/clojurescript "0.0-2268"]
                 [om "0.6.4"]
                 [cljs-ajax "0.2.6"]

                 ;; Clojure
                 [org.clojure/clojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [clj-http "0.9.2"]
                 [org.clojure/data.json "0.2.5"]
                 [enlive "1.1.5"]
                 [compojure "1.1.8"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.11"]]

  :ring {:handler cljg.handler/app}

  :cljsbuild {:builds
              [{:source-paths ["src-cljs"]
                ;; Google Closure (CLS) options config
                :compiler {
                           :output-to "resources/public/js/js.js"
                           ;; minimal js optimization directive
                           :optimizations :whitespace
                           :pretty-print true}}]}

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
