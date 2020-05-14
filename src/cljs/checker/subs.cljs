(ns checker.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::page-index
 (fn [db]
   (:page-index db)))
