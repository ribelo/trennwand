(defproject trennwand "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :plugins [[lein-jupyter "0.1.16"]]
            :dependencies [[org.clojure/clojure "1.9.0"]
                           [org.clojure/spec.alpha "0.1.143"]
                           [org.clojure/core.async "0.3.465"]
                           [org.clojure/data.csv "0.1.4"]
                           [org.clojure/math.combinatorics "0.1.4"]

                           [cheshire "5.8.0"]
                           [criterium "0.4.4"]
                           [com.taoensso/encore "2.93.0"]
                           [dk.ative/docjure "1.12.0"]
                           [clj-pdf "2.2.30"]
                           [kixi/stats "0.4.0"]
                           [net.cgrand/xforms "0.15.0"]
                           [funcool/cuerdas "2.0.4"]
                           ;[clj-time "0.13.0"]
                           ])
