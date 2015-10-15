(ns pubnub-cljs-example.main
    (:require [cljs.core.async :refer [chan <! >!]]
              [pubnub-cljs.core :as pubnub]
              [promesa.core :as p])
    (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                     [pubnub-cljs-example.macros :refer [env]]))

(enable-console-print!)


(def pubnub-client
  (pubnub/pubnub :publish-key (env "PUBNUB_PUBLISH_KEY")
                 :subscribe-key (env "PUBNUB_SUBSCRIBE_KEY")
                 :uuid "demouser"))

(def messages-tx (chan))
(def messages-rx (chan))
(def presence-rx (chan))

(p/then (pubnub/channel pubnub-client "demoroom"
          :messages-rx messages-rx
          :messages-tx messages-tx
          :presence-rx presence-rx)
  (fn [pubnub-channel]
    (do
      (go-loop []
        (when-let [incoming-message (<! messages-rx)]
          (if (= (:message incoming-message) ":quit")
            (let [history-channel (pubnub/history pubnub-channel)]
              (go-loop []
                (if-let [message (<! history-channel)]
                  (do
                    (println message)
                    (recur))
                  (pubnub/disconnect pubnub-channel))))
            (recur))))

      (go-loop []
        (when-let [presence-event (<! presence-rx)]
          (println presence-event)
          (recur)))

      (go (>! messages-tx "Hello, I have joined the channell?"))
      (go (>! messages-tx "Second message..."))
      (go (>! messages-tx ":quit"))))) ;; signal to stop processing
