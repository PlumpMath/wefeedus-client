(ns wefeedus-client.services
  (:require goog.net.WebSocket
            [domina.events :as domev]
            [io.pedestal.app.protocols :as p]
            [cljs.reader :as reader]
            [cljs.core.async :as async :refer [chan <! >! timeout close! put!]])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))


(defn client-connect! [address]
  (let [ctrl-ch (chan)
        rec-ch (chan)
        send-ch (chan)
        ws (goog.net.WebSocket.)]
    (doto ws
      (domev/listen! goog.net.WebSocket.EventType.OPENED
                     #(go (>! ctrl-ch "channel opened")))
      (domev/listen! goog.net.WebSocket.EventType.ERROR
                     #(go (>! ctrl-ch ["connection error" (.-evt %)])))
      (domev/listen! goog.net.WebSocket.EventType.MESSAGE
                     #(go
                       (let [recmsg (reader/read-string
                                     (.. % -evt -message))]
                         (.log js/console "received: " (str recmsg))
                         (>! rec-ch recmsg))))
      (.open (str "ws://" address)))
    (go (loop [m (str (<! send-ch))]
          (.log js/console "sending over ws" m)
          (.send ws m)
          (recur (str (<! send-ch)))))
    {:ctrl-ch ctrl-ch
     :rec-ch rec-ch
     :send-ch send-ch}))


(defrecord Services [app chans]
  p/Activity
  (start [this]
    (let [{:keys [ctrl-ch rec-ch send-ch]} chans]
      (go
       (.log js/console (<! ctrl-ch))
       #_(put! send-ch (str {:type :load-markers
                           :position {:lon 51 :lat 8}
                           :zoom 5
                           :hours [800 1320]
                           :date (js/Date.)}))
       (p/put-message (:input app) (<! rec-ch)))))
  (stop [this]))


(defn services-fn [{:keys [ctrl-ch rec-ch send-ch]} message input-queue]
  (.log js/console "sending" (str message))
  (put! send-ch message))
