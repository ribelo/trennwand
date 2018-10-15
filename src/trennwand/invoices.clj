(ns trennwand.invoices
  (:require [clojure.java.io :as io]
            [cuerdas.core :as str]))

(def invoices-dir_ (atom ""))
(def downloaded-invoices_ (atom #{}))


(defn set-invoices-dir! [dir]
  (reset! invoices-dir_ dir))


(defn get-downloaded-invoices []
  (into #{}
        (comp (filter #(.isFile %))
              (map #(-> % (.getName) (str/split ".") (first))))
        (file-seq (io/file @invoices-dir_))))


(defn check-downloaded-invoices! []
  (reset! downloaded-invoices_ (get-downloaded-invoices)))


