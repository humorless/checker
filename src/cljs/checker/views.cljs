(ns checker.views
  (:require
   [checker.config :as config]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [checker.subs :as subs]))

(defn atom-input
  " val-atom expect a reagent atom
    id expects a string
    input-type expect the type of the html5 input"
  [val-atom id input-type c]
  [:input {:type input-type
           :id id
           :class c
           :value @val-atom
           :on-change (fn [e]
                        (reset! val-atom (-> e .-target .-value))
                        (when config/debug?
                          (println @val-atom)))}])

(defn atom-textarea
  " val-atom expect a reagent atom
    id expects a string"
  [val-atom id c]
  [:textarea {:id id
              :class c
              :rows "5"
              :value @val-atom
              :on-change (fn [e]
                           (reset! val-atom (-> e .-target .-value))
                           (when config/debug?
                             (println @val-atom)))}])

(defn submit-button
  [bt-name data]
  [:input {:type "button"
           :value bt-name
           :onClick (fn [e]
                      (println data))}])

(defn input-page [p]
  [:main.helvetica.dark-gray
   [:div.mw8
    [:h1.tc "points checker"]
    [:table.collapse.w-100.mb2
     [:thead
      [:tr.bb
       [:th [:label.mr2 "category"]]
       [:th [:label.mr2 "url"]]
       [:th [:label.mr2 "code"]]]]
     [:tbody
      [:tr.bb
       [:td
        [:label.mr2  "LP"]]
       [:td
        [atom-input (reagent/atom "") "lp-url" "text"]]
       [:td
        [atom-textarea (reagent/atom "") "lp-code"]]]
      [:tr.bb
       [:td
        [:label.mr2  "CV"]]
       [:td
        [atom-input (reagent/atom "") "cv-url" "text"]]
       [:td
        [atom-textarea (reagent/atom "") "cv-code"]]]]]
    [:div.mv2
     [submit-button "check" {:a 1 :b 2}]]]])

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
