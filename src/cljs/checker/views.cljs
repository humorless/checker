(ns checker.views
  (:require
   [checker.config :as config]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [checker.subs :as subs]
   [checker.events :as events]
   [checker.routes :as routes]
   [clojure.string :as string]))

;; Templates

(def cv-template "<!-- LINE Free Coins CV Tracking Code Start -->
<script type=\"text/javascript\">
// This is just a sample code.
// Please make modification according to your needs.

var freecoins_cvq = [
    {
       app: \"FREECOINS_XXFREEXX\",
       domain: \"DOMAIN\",
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
  [tmpl-str free domain order item price memo]
  (-> tmpl-str
      (string/replace-first #"XXFREEXX" free)
      (string/replace-first #"DOMAIN" domain)
      (string/replace-first #"ORDER" order)
      (string/replace-first #"ITEM" item)
      (string/replace-first #"PRICE" price)
      (string/replace-first #"MEMO" memo)))

;; State

(defonce state (reagent/atom {:r-switch "buy"
                              :free     ""
                              :domain   ""
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
      (let [{:keys [free domain order item price memo]} @state]
        (swap! state
               assoc :cv
               (replace-tmpl cv-template
                             free domain order item price memo))))))

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

(defn home-panel []
  (let [free-ph            "請輸入 FREECOINS_後五碼，如 17785 "
        domain-ph          "請輸入 domain name, 如 .example.com "
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
       [:div.dt-row
        [:label.dtc {:for "domain"} "Domain: "]
        [common-input "domain" :domain domain-ph "\\.([a-zA-Z0-9]([-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?\\.)?([a-zA-Z0-9]{1,2}([-a-zA-Z0-9]{0,252}[a-zA-Z0-9])?)\\.([a-zA-Z]{2,63})" true]
        [:div.dtc "請填入前置句點的網域名稱"]]
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

;;; Begin checker panel

(defn checker-panel []
  (let [default-url ""
        r-url (reagent/atom default-url)]
    (fn []
      (let [resp    @(re-frame/subscribe [::subs/check-url-resp])
            errors  @(re-frame/subscribe [::subs/check-url-errors])
            checking @(re-frame/subscribe [::subs/checking])
            trigger-check (fn [url]
                            (re-frame/dispatch [::events/check-url url]))]
        [:main.helvetica.dark-gray.ml3
         [:form {:on-submit #(do (.preventDefault %)
                                 (trigger-check @r-url))}
          [:h1 "CPA LP Tag Checker"]
          [:div {:class "mv3 dib"}
           [:label {:class "pa2 mt2 f6"
                    :for "url"} "LP URL"]]
          [:div {:class "mv3 dib"}
           [:input {:type "url"
                    :class "pa2 mt2 br2 b--black-20 ba f6"
                    :id "url"
                    :placeholder "http://..."
                    :on-change #(reset! r-url (-> % .-target .-value))}]]
          [:button {:type "submit"
                    :disabled (when checking true)
                    :class "pointer br2 ba b--black-20 bg-green pa2 ml1 mv1 bg-animate f6"}
           "Check"]
          [:div {:class "ml3 mv3 dib"} "* 提醒：檢測至多會等20秒。"]
          (when errors [:div errors])
          (when resp
            (when config/debug?
              (println "debug resp: " resp))
            (let [{:keys [result debug lfc5 rcmd]} resp]
              [:<>
               [:div
                [:div {:class "w-10 mv3 dib b bg-light-blue"} "埋設狀態"]
                [:div {:class "ml2 mv3 dib"}
                 [:div (str result " 且 " lfc5)]]]
               [:div
                [:div {:class "w-10 mv3 dib b bg-light-blue"} "Tag 字串"]
                [:div {:class "ml2 mv3 dib"}
                 [:div (pr-str debug)]]]
               [:div
                [:div {:class "w-10 mv3 dib b  bg-light-blue"} "建議"]
                [:div {:class "ml2 mv3 dib"}
                 [:div rcmd]]]]))]]))))

(defmulti panels identity)

(defmethod panels :home-panel [] [home-panel])
(defmethod panels :checker-panel [] [checker-panel])
(defmethod panels :default [] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    (panels @active-panel)))
