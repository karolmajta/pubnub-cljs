(ns pubnub-cljs.core
  (:require [clojure.string :refer [replace]]
            [cljs.core.async :refer [>! <! close! chan]]
            [cljsjs.pubnub]
            [promesa.core :as p])
   (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; ----------------------------------------------------------------------------
;; PROTOCOL DEFINITIONS
;; ----------------------------------------------------------------------------

(defprotocol IPubNubChannel
  (disconnect [this])
  (history [this] [this count] [this count tx start]))


;; ----------------------------------------------------------------------------
;; IMPLEMENTATION
;; ----------------------------------------------------------------------------

(defn- to-obj [m]
  (let [sanitize-key (fn [s] (replace (name s) #"-" "_"))
        pairs (map #(vector (sanitize-key (first %)) (second %)) m)]
    (clj->js (into {} pairs))))


(defn- onmessage [tx message envelope channel timer magic-channel]
  (go (>! tx {:message message
              :timestamp (/ (aget envelope 1) 10000000)})))


(defn- onpresence [tx presence-event]
  (go (>! tx (js->clj presence-event :keywordize-keys true))))


(defn- onhistory [tx history-page]
  (doseq [message history-page]
    (go (>! tx message))))


(deftype PubNubChannel [pubnub name chan-map]
  IPubNubChannel
  (disconnect [this]
      (.unsubscribe pubnub name))

  (history [this]
    (history this nil))
  (history [this count]
    (history this count (chan count) nil))
  (history [this count tx start]
    (do
      (.history pubnub  (to-obj {
        :channel name
        :count count
        :start start
        :callback #(let [next-start (aget % 1)
                         history-page (reverse (aget % 0))]
                    (if (> next-start 0)
                      (do
                        (onhistory tx history-page)
                        (history this count tx next-start))))}))
      tx)))


(defn pubnub [& kwargs]
  (let [config (to-obj (apply hash-map kwargs))]
      (js/PUBNUB config)))


(defn channel [pubnub name & chans]
  (let [chan-map (apply hash-map chans)
        messages-rx (:messages-tx chan-map)
        messages-tx (:messages-rx chan-map)
        presence-tx (:presence-rx chan-map)]
    (do
      (go-loop []
        (when-let [published-message (<! messages-rx)]
          (.publish pubnub (to-obj {
            :channel name
            :message published-message}))
            (recur)))
            ;; TODO: we should add `callback` here and react to message
            ;; delivery errors...

      (p/promise (fn [resolve reject]
        (do
          (.subscribe pubnub (to-obj {
            :channel name
            :connect #(resolve (PubNubChannel. pubnub name chan-map))
            :message (partial onmessage messages-tx)
            :presence (partial onpresence presence-tx)}))))))))
