(ns checker.subs
  (:require
   [clojure.walk :as walk]
   [clojure.string :as string]
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

(defn interpret-lfc5 [m]
  (let [resp (walk/keywordize-keys m)]
    (if (:exist-lfc5 resp)
      {:lfc5 "找到 lfc5 Tag"}
      {:lfc5 "沒有找到 lfc5 Tag"})))

(defn remove-ctrl [e-tag]
  (->> e-tag
       (map #(string/replace % #"\n" ""))
       (mapv #(string/replace % #"\"" "'"))))

(defn interpret-tag [m]
  (let [{e-tag :exist-tag} (walk/keywordize-keys m)]
    (cond
      (and e-tag  (> (count e-tag) 1))
      {:result "埋設了兩組以上的CPA LP Tag" :debug (remove-ctrl e-tag) :rcmd "請確認哪一個Tag為本次從LINE取得的。(可看freecoins參數(5碼)數字)"}
      (and e-tag  (= (count e-tag) 1))
      {:result "埋設了一組CPA LP Tag" :debug (remove-ctrl e-tag) :rcmd "請確認freecoins參數(5碼)數字是否與LINE提供的相同。"}
      (or (nil? e-tag) (= (count e-tag) 0))
      {:result "沒有埋設/無法找到Tag" :rcmd "1.請埋設Tag。\n2.請確認是否有設置阻擋檢視原始碼等設定。\n3.請確認Tag埋設位置（不得位於Google Tag Manager等第三方追蹤內）"})))

(re-frame/reg-sub
 ::check-url-resp
 (fn [db]
   (when-let [e (get-in db [:check-url :resp])]
     (merge (interpret-lfc5 e)
            (interpret-tag e)))))
