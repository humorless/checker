(ns checker.events
  (:require
   [re-frame.core :as re-frame]
   [checker.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
            (assoc db :active-panel active-panel)))

;; --- Post check URL
(def endpoint "http://10.20.30.40:3000/api/check-url/tags")

(re-frame/reg-event-fx
 ::check-url
 (fn-traced [{db :db} [_ url]]
            {:db  (-> db
                      (assoc-in [:checking] true)
                      (assoc-in [:check-url :resp] nil)
                      (assoc-in [:errors :check-url] nil))
             :http-xhrio {:method :post
                          :uri endpoint
                          :params {:url url}
                          :timeout 16000
                          :format (ajax/json-request-format)
                          :response-format (ajax/json-response-format {:keyword? true})
                          :on-success [::check-url-success]
                          :on-failure [::api-request-error :check-url]}}))

(re-frame/reg-event-db
 ::api-request-error                          ;; triggered when we get request-error from the server
 (fn-traced [db [_ request-type response]]    ;; destructure to obtain request-type and response
            (-> db                            ;; when we complete a request we need to clean so that our ui is nice and tidy
                (assoc-in [:errors request-type] response)
                (assoc-in [:checking] false))))

(re-frame/reg-event-fx
 ::check-url-success
 (fn-traced [{db :db} [_ resp]]
            {:db         (-> db
                             (assoc-in [:check-url :resp] resp)
                             (assoc-in [:checking] false))
             :dispatch-n [[::complete-request :check-url]]}))

(re-frame/reg-event-fx
 ::complete-request
 (fn-traced [cofx [_ _]]
            {}))
