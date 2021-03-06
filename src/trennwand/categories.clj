(ns trennwand.categories
  (:refer-clojure :exclude [parents ancestors])
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.encore :as e]
            [clj-time.core :as dt]
            [clj-time.coerce :as dtc]
            [dk.ative.docjure.spreadsheet :as xls]
            [cuerdas.core :as str]
            [taoensso.encore :as e]
            [net.cgrand.xforms :as x]
            [kixi.stats.core :as kixi]
            [criterium.core :refer [quick-bench]]))


(def raw-categories
  {"01"     "alkohole",
   "0101"   "wino",
   "010101" "cydry",
   "010105" "wina musujące",
   "010106" "wina owocowe",
   "010107" "wina spokojne",
   "0103"   "piwo",
   "010301" "piwo w butelce",
   "010302" "piwo w puszce",
   "0104"   "alkohole mocne",
   "010402" "drinki",
   "010404" "whisky",
   "010405" "wódka",
   "02"     "chemia",
   "0202"   "kosmetyki",
   "020201" "artykuły sezonowe, kosmetyki",
   "020202" "dezodoranty i perfumy",
   "020203" "golenie",
   "020204" "makijaż",
   "020205" "mydła",
   "020206" "pasty i higiena jamy ustnej",
   "020207" "pielęgnacja ciała i twarzy",
   "020208" "płyny/żele do kąpieli",
   "020209" "preparaty do farbowania włosów",
   "020210" "preparaty do higieny intymnej",
   "020211" "preparaty do układania włosów",
   "020212" "szampony i odżywki",
   "020213" "kosmetyki migracja",
   "0203"   "odświeżacze",
   "020301" "odświeżacze",
   "0204"   "sezonowe owady",
   "0205"   "środki do czyszczenia",
   "020501" "środki do czyszczenia",
   "0206"   "detergenty do prania",
   "020601" "detergenty do prania",
   "0207"   "detergenty do zmywania",
   "020701" "detergenty do zmywania",
   "0208"   "higiena osobista",
   "020801" "chusteczki i ręczniki",
   "020802" "higiena niemowląt ",
   "020803" "papier toaletowy",
   "020805" "podpaski",
   "020806" "tampony",
   "03"     "strefa przykasowa",
   "0301"   "papierosy",
   "030101" "akcesoria do papierosów",
   "030103" "papierosy",
   "030104" "tytoń",
   "0302"   "prasa",
   "030201" "prasa",
   "0304"   "farmacja",
   "030401" "farmacja",
   "0305"   "telefonia i usługi",
   "030501" "telefonia",
   "04"     "mięso, wędliny i inne lada",
   "0401"   "mięso białe waga",
   "040102" "indyk waga",
   "040103" "kaczka waga",
   "040105" "kurczak waga",
   "0402"   "ryby waga",
   "040201" "marynaty rybne waga",
   "040203" "ryby świeże waga",
   "040204" "ryby wędzone waga",
   "0403"   "sery waga",
   "040301" "sery pleśniowe waga",
   "040302" "sery premium waga",
   "040303" "sery żółte waga",
   "040304" "twarogi waga",
   "040305" "sery waga przecena",
   "0404"   "wędliny waga",
   "040401" "kabanosy waga",
   "040403" "kiełbasy cienkie waga",
   "040404" "kiełbasy grube waga",
   "040405" "mielonki, bloki waga",
   "040406" "parówki, parówkowe i serdelki waga",
   "040407" "salami waga",
   "040408" "wędzonki waga",
   "040409" "wyroby podrobowe i garmażeryjne waga",
   "040410" "wędliny waga przecena",
   "0406"   "garmażerka waga",
   "040602" "dania mięsne waga",
   "040604" "dania warzywne waga",
   "0407"   "mięso czerwone waga",
   "040701" "cielęcina waga",
   "040703" "wieprzowina waga",
   "040704" "wołowina waga",
   "05"     "mrożonki",
   "0502"   "mrożonki",
   "050201" "dania mrożone",
   "050203" "lody",
   "050205" "owoce i warzywa mrożone",
   "050206" "ryby i owoce morza mrożone",
   "06"     "nabiał i chłodzone samoobsługa",
   "0601"   "jaja",
   "060101" "jaja",
   "0602"   "sery samoobsługa",
   "060201" "sery pleśniowe pakowane",
   "060202" "sery topione pakowane",
   "060203" "sery żółte pakowane",
   "060204" "twarogi pakowane",
   "0603"   "tłuszcze",
   "060301" "margaryna kulinarna",
   "060302" "margaryna stołowa",
   "060303" "masło",
   "060304" "mixy",
   "060305" "olej",
   "060307" "smalec",
   "0606"   "garmażerka samoobsługa",
   "060601" "dania mączne pakowane",
   "060602" "dania mięsne pakowane",
   "060603" "dania rybne pakowane",
   "060604" "marynaty i sałatki rybne pakowane",
   "060605" "wędliny pakowane",
   "0608"   "produkty mleczne",
   "060801" "desery, serki, kefiry",
   "060802" "jogurty ",
   "060803" "mleko świeże",
   "060804" "mleko uht",
   "060805" "śmietany",
   "07"     "napoje, soki",
   "0701"   "woda",
   "070101" "woda",
   "0703"   "napoje, soki",
   "070301" "napoje gazowane inne",
   "070302" "napoje typu cola",
   "070303" "napoje typu ice tea",
   "070304" "napoje typu izotoniki, energetyki",
   "070305" "soki, nektary, inne napoje niegazowane",
   "070306" "syropy",
   "08"     "kosztowe",
   "0802"   "artykuły kosztowe",
   "080201" "artykuły kosztowe",
   "0803"   "opakowania",
   "080301" "opakowania",
   "09"     "owoce i warzywa",
   "0902"   "kiszonki i koncentraty",
   "0903"   "kwiaty i rośliny",
   "090201" "kiszonki i koncentraty",
   "0904"   "owoce i warzywa pakowane",
   "090401" "owoce pakowane",
   "090402" "warzywa pakowane",
   "090403" "grzyby suszone pakowane",
   "0905"   "owoce luz",
   "090501" "bakalie",
   "090502" "cytrusy",
   "090503" "jabłka i gruszki",
   "090504" "melony",
   "090505" "owoce egzotyczne",
   "090508" "winogrona",
   "0906"   "sałaty i zioła",
   "090601" "sałaty pakowane",
   "0907"   "warzywa luz",
   "090701" "grzyby",
   "090702" "warzywa kapustne",
   "090703" "warzywa okopowe",
   "090704" "warzywa sałatkowe",
   "090706" "zieleniny",
   "10"     "pieczywo",
   "1002"   "pieczywo",
   "100201" "pieczywo  - pieczywo tostowe",
   "100202" "pieczywo - bułka tarta",
   "100203" "pieczywo - torilla",
   "100204" "pieczywo mrożone - bułki, bagietki",
   "100205" "pieczywo mrożone - ciasta",
   "100206" "pieczywo mrożone - ciasta",
   "100207" "pieczywo mrożone - przekąski",
   "100208" "pieczywo regionalne - bułki i bagietki",
   "100209" "pieczywo regionalne - chleb biały",
   "100210" "pieczywo regionalne - chleb ciemny",
   "100212" "pieczywo regionalne - przekąski słodkie i słone",
   "11"     "przemysłowe",
   "1102"   "artykuły przemysłowe",
   "110201" "akcesoria gospodarstwa domowego",
   "110202" "artykuły przemysłowe inne",
   "110203" "sezonowe grillowe",
   "110204" "sezonowe świąteczne",
   "110205" "szkoła",
   "110206" "znicze",
   "12"     "spożywka pakowana",
   "1201"   "przekąski",
   "120101" "przekąski",
   "1203"   "śniadania, zdrowa żywność",
   "120301" "dżemy",
   "120302" "kakao i napoje czekoladowe",
   "120303" "płatki śniadaniowe",
   "120304" "zdrowa żywność",
   "1206"   "herbata",
   "120601" "herbaty czarne",
   "120602" "herbaty pozostałe",
   "1207"   "kulinaria słona",
   "120701" "buliony",
   "120702" "dania gotowe",
   "120703" "konserwy mięsne",
   "120704" "konserwy rybne",
   "120705" "ocet",
   "120706" "pasztety",
   "120707" "produkty z pomidorów",
   "120708" "przyprawy",
   "120709" "sosy mokre",
   "120710" "sosy w proszku",
   "120711" "warzywa konserwowe",
   "120712" "zupy",
   "1208"   "kawa",
   "120801" "kawy mielone",
   "120802" "kawy rozpuszczalne",
   "120803" "kawy ziarniste",
   "1209"   "kulinaria słodka",
   "120901" "bakalie pakowane",
   "120902" "desery w proszku",
   "120903" "dodatki do ciast",
   "120904" "owoce w puszkach",
   "1210"   "produkty spożywcze podstawowe",
   "121001" "cukier",
   "121002" "kasza",
   "121003" "makaron",
   "121004" "mąka",
   "121005" "ryż",
   "121006" "zabielacze do kawy",
   "1211"   "słodycze",
   "121101" "batony",
   "121102" "bombonierki",
   "121103" "chałwa i sezamki",
   "121104" "ciastka",
   "121105" "cukierki pakowane",
   "121106" "czekolady",
   "121107" "gumy do żucia",
   "121108" "lizaki",
   "121109" "słodycze luz",
   "121111" "słodycze świąteczne",
   "1212"   "dania dla niemowląt",
   "121201" "dania dla niemowląt",
   "13"     "dla zwierząt",
   "1301"   "karma i akcesoria dla zwierząt",
   "130102" "karma dla kotów",
   "130103" "karma dla psów",
   "130105" "piasek, żwirek",
   "14"     "ogólna migracja",
   "1401"   "ogólna migracja",
   "140101" "ogólna migracja"})


(def parents
  (into {}
        (comp 
         (filter (fn [[k v]]
                   (= 2 (count k)))))
        raw-categories))


(defn ancestors [idx]
  (into {} (filter (fn [[k v]]
                     (and (str/starts-with? k idx)
                          (= (count k) (+ (count idx) 2))))) 
        raw-categories))


(defn tree 
  ([idx]
   (when idx
     {:idx idx
      :name (get idx raw-categories)
      :children 
      (mapv (fn [[k v]]
              {:idx k
               :name v
               :children (tree k)})
            (ancestors idx))}))
  ([]
   (mapv (fn [[k v]] (tree k)) parents)))


(defn make-series [key coll]
  (into [] (comp 
            (filter (fn [{:keys [category-id]}] (str/starts-with? category-id "12")))
            (x/by-key (fn [{:keys [category-id date]}] 
                        [(str/slice category-id 0 4)
                         (dt/first-day-of-the-month date)]) 
                      (comp (map key) (x/reduce +)))
            (map (fn [[[c d] sales]] 
                   [(dtc/to-string d) 
                    (long sales)
                    (get raw-categories c)]))
            ) 
        coll))