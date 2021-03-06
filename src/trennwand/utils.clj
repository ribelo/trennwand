(ns trennwand.utils
  (:require [clojupyter.misc.display :as display]))

(defn vmap->table
  ([ks coll]
   (display/hiccup-html
     (let [ks (or ks (keys (first coll)))]
       [:table
        [:tr (doall
               (for [k ks]
                 [:th (if (vector? k) (name (second k)) (name k))]))]
        (doall
          (for [m coll]
            [:tr (doall
                   (for [k ks]
                     [:td (str (get m (if (vector? k) (first k) k) "nil"))]))]))])))
  ([coll]
   (vmap->table coll nil)))