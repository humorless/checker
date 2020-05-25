(ns checker.views
  (:require
   [checker.config :as config]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [checker.subs :as subs]
   [clojure.string :as string]))

(defn switch [id val-atom]
  [:select {:id id :on-change (fn [e]
                                (reset! val-atom (-> e .-target .-value))
                                (when config/debug?
                                  (println "debug the value of seletion:" @val-atom)))}
   [:option {:value "buy"} "購買型任務"]
   [:option {:value "non-buy"} "非購買型任務"]])

(def cv-template "<!-- LINE Free Coins CV Tracking Code Start -->
<script type=\"text/javascript\">


var freecoins_cvq = [
    {
       app: \"FREECOINS_XXFREEXX\",
       cv: [
            {
                action: \"REGISTRATION\",
                order: \"ORDER\",
                item: \"ITEM\",
                t_price: \"PRICE\",
                quantity: \"1\",
                memo: \"MEMO\"
            }
       ]
    }
];

</script>
<script src=\"https://freecoins.line-apps.com/lfc5.js\" async>
</script>
<!-- LINE Free Coins CV Tracking Code End -->")

(defonce r-switch (reagent/atom "buy"))

(defonce free-atom (reagent/atom ""))
(defonce order-atom (reagent/atom ""))
(defonce item-atom (reagent/atom ""))
(defonce price-atom (reagent/atom ""))
(defonce memo-atom (reagent/atom ""))
(defonce cv-atom (reagent/atom cv-template))

(defn common-input [id val-atom ph]
  [:input.dtc {:id id
               :size 60
               :placeholder ph
               :on-change (fn [e]
                            (reset! val-atom (-> e .-target .-value))
                            (when config/debug?
                              (println "debug common-input: " @val-atom)))}])

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
     [:div.dt-row
      [:label.dtc {:for "order"} "Order: "] [common-input "order" order-atom order-ph]
      [:div.dtc "max 128 bytes (僅限填寫英文/數字，全形符號視為2~3bytes)"]]
     [:div.dt-row
      [:label.dtc {:for "item"} "Item: "] [common-input "item" item-atom item-ph]
      [:div.dtc "max 255 bytes (僅限填寫英文/數字，全形符號視為2~3bytes)"]]
     [:div.dt-row
      [:label.dtc {:for "t-price"} "t price: "] [common-input "t-price" price-atom price-ph]
      [:div.dtc "max 12 位數字 (可接受小數點後2位)，請勿使用千分位符號"]]]))

(defn pre-block [data]
  [:pre.mw7.pa3.ba.br2.b--black.h7.bg-white-20.hljs
   {:id "code"}
   data])

(defn escape [in]
  (str "\"" in  "\""))

(defn replace-tmpl
  [tmpl-str free order item price memo]
  (-> tmpl-str
      (string/replace-first #"XXFREEXX" free)
      (string/replace-first #"ORDER" order)
      (string/replace-first #"ITEM" item)
      (string/replace-first #"PRICE" price)
      (string/replace-first #"MEMO" memo)))

(defn input-page [p]
  (let [free-ph  "請輸入 FREECOINS_後五碼，如 17785 "
        memo-ph  "選填額外資訊，如：促銷註記"]
    [:main.helvetica.dark-gray.ml3
     [:form
      [:h1 "CPA CV Tag Format Generator"]
      [:div.dt--fixed
       [:div.dt-row
        [:label.dtc {:for "switch"} "任務類型: "] [switch "switch" r-switch]]
       [:div.dt-row
        [:label.dtc {:for "freecoin"} "Freecoins 參數: "]
        [common-input "freecoin" free-atom free-ph]
        [:div.dtc]]
       [div-middle @r-switch]
       [:div.dt-row
        [:label.dtc {:for "memo"} "Memo: "]
        [common-input "memo" memo-atom memo-ph]
        [:div.dtc "max 255 bytes"]]]
      [:div
       [:div]
       [:div
        [:input {:type "submit" :value "generate"
                 :on-click (fn [e]
                             (.. e preventDefault)
                             (prn (-> e .-target .-value))
                             (reset! cv-atom
                                     (replace-tmpl cv-template
                                                   @free-atom @order-atom @item-atom
                                                   @price-atom @memo-atom)))}]]]]
     [:div.flex.items-center
      [:label.mr5
       {:for "code"}
       "CV code"]
      [pre-block @cv-atom]]]))

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
