(ns wefeedus-client.simulated.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.util.platform :as platform]))


;; Implement services to simulate talking to back-end services

(defn add-markers [t [lon lat] input-queue]
  (let [start (rand-nth (range 480 1320))
        end (+ start (rand-nth (range 30 180)))
        date (js/Date. (+ (.getTime (js/Date.))
                          (* (- (rand 10) 5)
                             24 3600 1000)))]
    (p/put-message input-queue {msg/type :load-markers
                                msg/topic [:markers]
                                :markers #{{:lon (+ lon (- (rand) 0.5))
                                            :lat (+ lat (- (rand) 0.5))
                                            :type (rand-nth [:soup :meal :cake])
                                            :date date
                                            :start start
                                            :end end}}}))
  (platform/create-timeout t #(add-markers t [lon lat] input-queue)))

(defrecord MockServices [app]
  p/Activity
  (start [this]
    (add-markers 5000 [8.1 49.1] (:input app)))
  (stop [this]))
