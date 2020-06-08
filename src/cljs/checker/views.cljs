(ns checker.views
  (:require
   [checker.config :as config]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [checker.subs :as subs]
   [clojure.string :as string]))

;; Templates

(def cv-template "<!-- LINE Free Coins CV Tracking Code Start -->
<script type=\"text/javascript\">
// This is just a sample code.
// Please make modification according to your needs.

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

(defn replace-tmpl
  [tmpl-str free order item price memo]
  (-> tmpl-str
      (string/replace-first #"XXFREEXX" free)
      (string/replace-first #"ORDER" order)
      (string/replace-first #"ITEM" item)
      (string/replace-first #"PRICE" price)
      (string/replace-first #"MEMO" memo)))

;; State

(defonce state (reagent/atom {:r-switch "buy"
                              :free     ""
                              :order    ""
                              :item     ""
                              :price    ""
                              :memo     ""
                              :cv       cv-template}))

;; State handlers

(defn handle-switch-change
  [e]
  (swap! state assoc :r-switch (-> e .-target .-value))
  (when config/debug?
    (println "debug the value of seletion:" @state)))

(defn handle-input-change
  [entity-key e]
  (swap! state
         assoc entity-key (-> e .-target .-value))
  (when config/debug?
    (println "debug common-input: " @state)))

(defn handle-form-submit
  [e]
  (.. e preventDefault)
  (let [chk (-> js/document (.getElementById "form-id") (.checkValidity))]
    (println "form validation result: " chk)
    (-> js/document (.getElementById "form-id") (.reportValidity))
    (when chk
      (let [{:keys [free order item price memo]} @state]
        (swap! state
               assoc :cv
               (replace-tmpl cv-template
                             free order item price memo))))))

(defn copy-to-clipboard [v]
  (let [el (js/document.createElement "textarea")]
    (set! (.-value el) v)
    (.appendChild js/document.body el)
    (.select el)
    (js/document.execCommand "copy")
    (.removeChild js/document.body el)))

;; UI elements


(defn switch [id]
  [:select {:id        id
            :value     (:r-switch @state)
            :on-change handle-switch-change}
   [:option {:value "buy"} "購買型任務"]
   [:option {:value "non-buy"} "非購買型任務"]])

(defn common-input [id entity-key ph pattern req]
  [:input.dtc {:id          id
               :size        60
               :placeholder ph
               :required    req
               :pattern     pattern
               :value       (entity-key @state)
               :on-change   (partial handle-input-change entity-key)}])

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
      [:label.dtc {:for "order"} "Order: "] [common-input "order" :order order-ph "\\w{1,128}" true]
      [:div.dtc "max 128 bytes (僅限填寫半形英文/數字)"]]
     [:div.dt-row
      [:label.dtc {:for "item"} "Item: "] [common-input "item" :item item-ph "\\w{1,255}" true]
      [:div.dtc "max 255 bytes (僅限填寫半形英文/數字)"]]
     [:div.dt-row
      [:label.dtc {:for "t-price"} "t price: "] [common-input "t-price" :price price-ph "\\d{1,12}|\\d{1,11}\\.\\d|\\d{1,10}\\.\\d{2}" true]
      [:div.dtc "max 12 位數字 (可接受小數點後2位) ，請勿使用千分位符號"]]]))

(defn pre-block [data]
  [:pre.mw7.pa3.ba.br2.b--black.h7.bg-white-20.hljs
   {:id "code"}
   data])

(defn escape [in]
  (str "\"" in  "\""))

(defn input-page [p]
  (let [free-ph            "請輸入 FREECOINS_後五碼，如 17785 "
        memo-ph            "選填額外資訊，如：促銷註記"
        {:keys [cv
                r-switch]} @state]
    [:main.helvetica.dark-gray.ml3
     [:form {:id "form-id"}
      [:h1 "CPA CV Tag Format Generator"]
      [:div.dt--fixed
       [:div.dt-row
        [:label.dtc {:for "switch"} "任務類型: "] [switch "switch"]]
       [:div.dt-row
        [:label.dtc {:for "freecoin"} "Freecoins 參數: "]
        [common-input "freecoin" :free free-ph "\\d{5}" true]
        [:div.dtc "請填 5 碼數字"]]
       [div-middle r-switch]
       [:div.dt-row
        [:label.dtc {:for "memo"} "Memo: "]
        [common-input "memo" :memo memo-ph ".*" false]
        [:div.dtc "max 255 bytes"]]]
      [:div
       [:div]
       [:div
        [:input {:type     "submit"
                 :value    "generate"
                 :on-click handle-form-submit}]
        [:input.ml4 {:type     "button"
                     :value    "copy"
                     :on-click #(do (.stopPropagation %)
                                    (copy-to-clipboard (:cv @state)))}]]]]
     [:div.flex.items-center
      [:label.mr5
       {:for "code"}
       "CV code"]
      [pre-block cv]]]))

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
