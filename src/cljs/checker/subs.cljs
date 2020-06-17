(ns checker.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-panel
 (fn [db]
   (:active-panel db)))

(re-frame/reg-sub
 ::checking
 (fn [db]
   (:checking db)))

(re-frame/reg-sub
 ::check-url-errors
 (fn [db]
   (when-let [e (get-in db [:errors :check-url])]
     (pr-str e))))

(re-frame/reg-sub
 ::check-url-resp
 (fn [db]
   (when-let [e (get-in db [:check-url :resp])]
     (pr-str e))))
