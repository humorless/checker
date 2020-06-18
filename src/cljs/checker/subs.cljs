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
      {:lfc5 "lfc5 found"}
      {:lfc5 "lfc5 not found"})))

(defn remove-ctrl [e-tag]
  (->> e-tag
       (map #(string/replace % #"\n" ""))
       (mapv #(string/replace % #"\"" "'"))))

(defn interpret-tag [m]
  (let [{e-tag :exist-tag} (walk/keywordize-keys m)]
    (cond
      (and e-tag  (> (count e-tag) 1))
      {:result "Multiple tags found" :debug (remove-ctrl e-tag) :rcmd "There are multiple tags installed. Please install the only correct one."}
      (and e-tag  (= (count e-tag) 1))
      {:result "Single tag found" :debug (remove-ctrl e-tag) :rcmd "Please check if the Freecoin 5 digits is correct"}
      (or (nil? e-tag) (= (count e-tag) 0))
      {:result "tag not found" :rcmd "Please double check if Tag is correctly installed"})))

(re-frame/reg-sub
 ::check-url-resp
 (fn [db]
   (when-let [e (get-in db [:check-url :resp])]
     (merge (interpret-lfc5 e)
            (interpret-tag e)))))
