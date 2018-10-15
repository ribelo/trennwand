(ns trennwand.data
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [dk.ative.docjure.spreadsheet :as xls]
            [cuerdas.core :as str]
            [taoensso.encore :as e]
            [clj-time.core :as dt]
            [clj-time.coerce :as dtc]
            [clj-time.format :as dtf]
            [clj-time.periodic :as dtper]
            [net.cgrand.xforms :as x]
            [kixi.stats.core :as kixi]
            [criterium.core :refer [quick-bench]]))

(defn load-cg-mag []
  (->> (xls/load-workbook-from-file "/home/ribelo/Sync/schowek/rk/cg-tmp1.xls")
       (xls/select-sheet "Magazyn  Magazyn  0")
       (xls/select-columns {:A :product/name
                            :B :product/plu
                            :C :price/buy
                            :D :VAT})
       (rest)
       (map (fn [m]
              (as-> m $
                (update $ :VAT #(condp = %
                                  "5 %" 0.05
                                  "8 %" 0.08
                                  "23 %" 0.23
                                  0.0)))))))

(defn load-dc1-mag []
  (->> (xls/load-workbook-from-file "/home/ribelo/Sync/schowek/rk/dc1-tmp1.xls")
       (xls/select-sheet "Arkusz 1")
       (xls/select-columns {:A :product/name
                            :B :product/plu
                            :C :sales/pace
                            :D :state/optimal
                            :E :product/supplier
                            :F :price/buy
                            :G :state/current})
       (rest)
       (map (fn [m]
              (as-> m $
                (update $ :product/name (fn [s] (apply str (drop 10 s))))
                (update $ :sales/pace (fn [p] (e/round2 p)))
                (update $ :price/buy (fn [p] (e/round2 p)))
                (update $ :state/optimal (fn [p] (try
                                                   (double (e/round p))
                                                   (catch Exception e
                                                     0.0)))))))))


(defn load-dc2-mag []
  (->> (xls/load-workbook-from-file "/home/ribelo/Sync/schowek/rk/dc2-tmp1.xls")
       (xls/select-sheet "Arkusz 1")
       (xls/select-columns {:A :product/name
                            :B :product/plu
                            :C :sales/pace
                            :D :state/optimal
                            :E :product/supplier
                            :F :price/buy
                            :G :state/current})
       (rest)
       (map (fn [m]
              (as-> m $
                (update $ :product/name (fn [s] (apply str (drop 10 s))))
                (update $ :sales/pace (fn [p] (e/round2 p)))
                (update $ :price/buy (fn [p] (e/round2 p)))
                (update $ :state/optimal (fn [p] (try
                                                   (double (e/round p))
                                                   (catch Exception e
                                                     0.0)))))))))

(defn load-dc1-docs []
  (->> (xls/load-workbook-from-file "/home/ribelo/Sync/schowek/rk/dc1-pz.xls")
       (xls/select-sheet "Arkusz 1")
       (xls/select-columns {:A :doc/date
                            :C :doc/type
                            :D :doc/id})
       (rest)))


(defn load-dc2-docs []
  (->> (xls/load-workbook-from-file "/home/ribelo/Sync/schowek/rk/dc2-pz.xls")
       (xls/select-sheet "Arkusz 1")
       (xls/select-columns {:A :doc/date
                            :C :doc/type
                            :D :doc/id})
       (rest)))

(defn read-csv-data
  [path & {:keys [separator quote encoding]
           :or   {separator \;
                  quote     \~}}]
  (csv/read-csv (io/reader path :encoding (or encoding "UTF-8")) :separator separator :quote quote))


(defn map->rows
  ([columns data]
   (let [headers (map name columns)
         rows (mapv #(mapv % columns) data)]
     (cons headers rows)))
  ([data]
   (map->rows (keys (first data)) data)))


(defn write-csv-data
  ([path ks data]
   (with-open [out-file (io/writer path)]
     (csv/write-csv out-file (map->rows (if (seq ks) ks (keys (first data))) data)
                    :separator \;
                    :encoding "utf8")))
  ([path data]
   (with-open [out-file (io/writer path)]
     (csv/write-csv out-file (map->rows (keys (first data)) data)
                    :separator \;
                    :encoding "utf8"))))


(defn read-s1-sales
  ([date]
   (let [date-str (dtf/unparse (dtf/formatter "YYYY_MM_dd") date)
         path (str "/home/ribelo/s1-dane/F01451_StoreSale_" date-str)]
     (if (.exists (io/as-file path))
       (-> (read-csv-data path :encoding "windows-1250")
           (->> (mapv (fn [[_ date ean _ id name _ _ category qty _ sales _ profit _ _ _ unit vat _ _ weight category-id]]
                        {:date        (dtc/from-string date)
                         :ean         ean
                         :id          id
                         :category-id category-id
                         :name        name
                         :category    category
                         :qty         (Double/parseDouble qty)
                         :sales       (Double/parseDouble sales)
                         :profit      (Double/parseDouble profit)
                         :weight      weight}))))
       [])))
  ([start end]
   (let [days (dtper/periodic-seq start end (dt/days 1))]
     (->> (mapv read-s1-sales days)
          (flatten)))))


(defn read-s3-sales
  ([date]
   (let [date-str (dtf/unparse (dtf/formatter "YYYY_MM_dd") date)
         path (str "/home/ribelo/s3-dane/F01450_StoreSale_" date-str)]
     (if (.exists (io/as-file path))
       (-> (read-csv-data path :encoding "windows-1250")
           (->> (mapv (fn [[_ date ean _ id name _ _ category qty _ sales _ profit _ _ _ unit vat _ _ weight category-id]]
                        {:date        (dtc/from-string date)
                         :ean         ean
                         :id          id
                         :category-id category-id
                         :name        name
                         :category    category
                         :qty         (Double/parseDouble qty)
                         :sales       (Double/parseDouble sales)
                         :profit      (Double/parseDouble profit)
                         :weight      weight}))))
       [])))
  ([start end]
   (let [days (dtper/periodic-seq start end (dt/days 1))]
     (->> (mapv read-s3-sales days)
          (flatten)))))


;(def tmp-data
;  (->> (read-s1-sales (dt/date-time 2017 10 1) (dt/date-time 2017 11 1))))


;(def report
;  (into [] (comp
;             (map (fn [{:keys [date] :as m}] (assoc m :week-day (dt/day-of-week date))))
;             (x/by-key :date (x/transjuxt {:week-day (comp (map :week-day) (take 1))
;                                           :sales    (comp (map :sales) (x/reduce +))
;                                           :r1       (comp (filter #(= "Pieczywo regionalne - Chleb biały" (:category %)))
;                                                           (map :qty)
;                                                           (x/reduce +))
;                                           :r2       (comp (filter #(= "Pieczywo regionalne - Chleb ciemny" (:category %)))
;                                                           (map :qty)
;                                                           (x/reduce +))
;                                           :r3       (comp (filter #(= "Pieczywo regionalne - Bułki i bagietki" (:category %)))
;                                                           (map :qty)
;                                                           (x/reduce +))
;                                           :r4       (comp (filter #(= "Pieczywo regionalne - Przekąski słodkie i słone" (:category %)))
;                                                           (map :qty)
;                                                           (x/reduce +))
;                                           :r5       (comp (filter #(re-find #"(?iu)Pieczywo mrożone" (:category %)))
;                                                           (map :qty)
;                                                           (x/reduce +))
;                                           :r6       (comp (filter #(= "Słodycze luz" (:category %)))
;                                                           (map :sales)
;                                                           (x/reduce +))
;                                           :r7       (comp (filter #(re-find #"(?iu)Papierosy" (:category %)))
;                                                           (map :sales)
;                                                           (x/reduce +))}))
;             (x/sort-by first)
;             (map second)
;             (x/by-key :week-day (x/transjuxt {:week-day (comp (map :week-day) (take 1))
;                                               :sales    (comp (map :sales) (x/reduce kixi/median))
;                                               :r1       (comp (map :r1) (x/reduce kixi/median))
;                                               :r2       (comp (map :r2) (x/reduce kixi/median))
;                                               :r3       (comp (map :r3) (x/reduce kixi/median))
;                                               :r4       (comp (map :r4) (x/reduce kixi/median))
;                                               :r5       (comp (map :r5) (x/reduce kixi/median))
;                                               :r6       (comp (map :r6) (x/reduce kixi/median))
;                                               :r7       (comp (map :r7) (x/reduce kixi/median))}))
;             (map second)
;             (x/sort-by :week-day)
;             ;(map (fn [{:keys [sales r1 r2 r3 r4 r5]}]
;             ;       {:r1 (/ r1 sales)
;             ;        :r2 (/ r2 sales)
;             ;        :r3 (/ r3 sales)
;             ;        :r4 (/ r4 sales)
;             ;        :r5 (/ r5 sales)}))
;             ) tmp-data))


;(let [chart (ic/bar-chart (->> report (map :week-day)) (->> report (map :sales))
;                          :legend true :series-label "obrót" :x-label "dni tygodnia" :y-label "pln")]
;  (-> chart
;      ;(ic/add-categories (->> report (map :week-day)) (->> report (map :r2)) :series-label "razowe")
;      ;(ic/add-categories (range 1 8) (->> report (map :r2)) :series-label "razowe")
;      ;(ic/add-categories (range 1 8) (->> report (map :r4)) :series-label "słodkie")
;      ;(ic/add-categories (range 1 8) (->> report (map :r5)) :series-label "wypiek")
;      (i/view)))

;(->> tmp-data
;     (map :category)
;     (distinct))


