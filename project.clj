(defproject trennwand "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[com.github.k2n/lein-gorilla "-SNAPSHOT" ]]
  :repositories [["jitpack" "https://jitpack.io"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [clj-time "0.14.2"]
                 [cheshire "5.8.0"]
                 [criterium "0.4.4"]
                 [com.taoensso/encore "2.93.0"]
                 [org.apache.commons/commons-math3 "3.6.1"]
                 [dk.ative/docjure "1.12.0"]
                 [darwin "1.0.1"]
                 [lowl4tency/google-maps "0.3.0-SNAPSHOT"]
                 [clj-pdf "2.2.30"]
                 [kixi/gorilla-repl "0.4.1"]
                 [ring/ring-json "0.5.0-beta1" :exclusions [org.clojure/clojure]]
                 [compojure "1.6.0" :exclusions [ring/ring-core ring/ring-json]]
                 [cider/cider-nrepl "0.15.1" :exclusions [org.clojure/clojure]]
                 [huri "0.10.0-SNAPSHOT"]
                 [kixi/stats "0.4.0"]
                 ])
