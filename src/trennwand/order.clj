(ns trennwand.order
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [trennwand.data :as data]
            [taoensso.encore :as e]
            [net.cgrand.xforms :as x]
            [clj-pdf.core :as pdf]))


(defn find-product-by-plu [products {:keys [product/plu]}]
  (->> products
       (filter #(= plu (:product/plu %)))
       (first)))


(defn make-cg-prices [data]
  (let [cg (data/load-cg-mag)]
    (sequence
      (comp
        (map (fn [{:keys [product/plu price/buy] :as p}]
               (when-let [{price-cg :price/buy vat :VAT} (find-product-by-plu cg p)]
                 (when (and (number? buy) (number? price-cg) (pos? buy))
                   (let [diff-% (/ buy price-cg)]
                     (when (> diff-% 1.05)
                       (let [new-price (e/round2 (* price-cg (min diff-% 1.08) (+ 1 vat)))]
                         {:plu   plu
                          :price new-price})))))))
        (filter identity))
      data)))


(defn make-order [data]
  (let [cg (data/load-cg-mag)]
    (sequence
      (comp
        (map (fn [{:keys [product/plu product/name product/supplier price/buy
                          state/optimal state/current] :as p}]
               (let [price-cg (:price/buy (find-product-by-plu cg p))]
                 (when (or (re-find #"(?iu)rawicz|wieluń|asco|demi|mońki|mazur|salami" name)
                           (and (number? buy) (number? price-cg) (pos? buy)))
                   (-> p
                       (assoc :price/s1 buy
                              :price/cg price-cg
                              :diff/$ (when price-cg (e/round2 (- buy price-cg)))
                              :diff/% (when price-cg (e/round2 (- 1 (/ price-cg buy))))
                              :order/qty (e/round2 (- optimal (max 0.0 current))))
                       (dissoc :price/buy))))))
        (filter identity)
        (filter (fn [{:keys [diff/%]}] (or (nil? %) (> % 0.035))))
        (filter (fn [{:keys [order/qty]}] (pos? qty)))
        (x/sort-by :order/qty #(compare %2 %1)))
      data)))


(defn data->pdf [data path s]
  (pdf/pdf
    [{:title                  (str s " propozycja zamówienia teas")
      :right-margin           50
      :left-margin            10
      :bottom-margin          10
      :top-margin             10
      :size                   "a4"
      :orientation            :landscape
      :register-system-fonts? true
      :font                   {:encoding :unicode
                               :align    :center
                               :size     10
                               :ttf-name "Noto Sans"}}
     [:paragraph {:style :bold :size 14}
      (str s " - propozycja zamówienia teas")]
     [:spacer 3]
     (into [:table {:header ["plu" "nazwa"
                             ;"cena eurocash"
                             ;"cena teas"
                             "różnica zł" "różnica %"
                             "stan" "optymalny" "tempo" "zamówić"]
                    :widths [15 25 12 8 8 8 8 8]
                    }]
           (for [{:keys [product/plu product/name price/s1 price/cg diff/$ diff/%
                         state/current state/optimal sales/pace]
                  order :order/qty} data]
             [[:cell plu]
              [:cell name]
              ;[:cell (format "%.2f" s1)]
              ;[:cell (if cg (format "%.2f" cg) "brak")]
              [:cell (if $ (str (format "%.2f" $) " zł") "brak")]
              [:cell (if % (str (format "%.2f" (* 100 %)) "%") "brak")]
              [:cell (format "%.2f" current)]
              [:cell (format "%.2f" optimal)]
              [:cell (format "%.2f" pace)]
              [:cell (format "%.2f" order)]
              ]))]
    path))


(defn generate-order-map [dc]
  (let [data (case dc :dc1 (data/load-dc1-mag) :dc2 (data/load-dc2-mag))]
    (-> data
        (make-order))))


(defn generate-order-pdf [dc]
  (let [data (case dc :dc1 (data/load-dc1-mag) :dc2 (data/load-dc2-mag))
        dc' (case dc :dc1 "dc-1" :dc2 "dc-2")]
    (-> data
        (make-order)
        (data->pdf (str "/home/huxley/Sync/schowek/rk/zamowienie_cg-" dc' ".pdf") dc'))))


(defn generate-cg-prices []
  (->>
    (data/load-dc1-mag)
    (make-cg-prices)
    (data/write-csv-data "/home/huxley/Sync/schowek/rk/cg_prices.csv")))
