(ns checker.views
  (:require
   [checker.config :as config]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [checker.subs :as subs]))

(defn switch [id val-atom]
  [:select {:id id :on-change (fn [e]
                                (reset! val-atom (-> e .-target .-value))
                                (when config/debug?
                                  (println "debug the value of seletion:" @val-atom)))}
   [:option {:value "buy"} "購買型任務"]
   [:option {:value "non-buy"} "非購買型任務"]])

(defonce r-switch (reagent/atom "buy"))

(defonce free-atom (reagent/atom ""))
(defonce order-atom (reagent/atom ""))
(defonce item-atom (reagent/atom ""))
(defonce price-atom (reagent/atom ""))
(defonce memo-atom (reagent/atom ""))

(defn common-input [id i-atom ph]
  [:input.w-80 {:id id
                :placeholder ph}])

(defn ph-switch-order [s]
  (if (= s "buy")
    "請填入訂單編號，如： order123"
    "請填入自定義流水號，如： member123"))

(defn ph-switch-item [s]
  (if (= s "buy")
    "請填入購買商品ID，如：0000123"
    "請填入自定義項目ID，如：0000123"))

(defn ph-switch-price [s]
  (if (= s "buy")
    "請填入購買訂單總價，如：1200"
    "請填入自定義總價，如：1200"))

(defn div-middle [s]
  (let [order-ph (ph-switch-order s)
        item-ph (ph-switch-item s)
        price-ph (ph-switch-price s)]
    [:<>
     [:div.mw6.flex.justify-between
      [:label {:for "order"} "Order: "] [common-input "order" order-atom order-ph]]
     [:div.mw6.flex.justify-between
      [:label {:for "item"} "Item: "] [common-input "item" item-atom item-ph]]
     [:div.mw6.flex.justify-between
      [:label {:for "t-price"} "t price: "] [common-input "t-price" price-atom price-ph]]]))

(defn input-page [p]
  (let [free-ph  "請輸入 FREECOINS_後五碼，如 17785 "
        memo-ph  "選填額外資訊，如：促銷註記"]
    [:main.helvetica.dark-gray.ml3
     [:h1 "CPA CV Tag Format Checker"]
     [:div.mw6.flex.justify-between
      [:label {:for "switch"} "任務類型: "] [switch "switch" r-switch]]
     [:div.mw6.flex.justify-between
      [:label {:for "freecoin"} "Freecoins 參數: "]
      [common-input "freecoin" free-atom free-ph]]
     [div-middle @r-switch]
     [:div.mw6.flex.justify-between
      [:label {:for "memo"} "Memo: "]
      [common-input "memo" memo-atom memo-ph]]]))

(defn result-page []
  (prn "result page"))

(defn default-page []
  (prn "default page"))

(defn main-panel []
  (let [p-i @(re-frame/subscribe [::subs/page-index])]
    (prn "p-i is:" p-i)
    (case p-i
      0 [input-page p-i]
      1 [result-page p-i]
      [default-page])))
