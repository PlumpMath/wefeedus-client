(ns ^:shared wefeedus-client.behavior
    (:require [clojure.string :as string]
              [clojure.set :as set]
              [io.pedestal.app.messages :as msg]
              [io.pedestal.app :as app]))


(defn set-value-transform [old-value message]
  (:value message))

(defn load-markers [old msg]
  (set/union (or old #{}) (:markers msg)))

(defn visible-markers [_ {markers :markers [start end] :hours date :date}]
  (filter #(and (or (and (>= (:start %) start)
                         (<= (:start %) end))
                    (and (>= (:end %) start)
                         (<= (:end %) end)))
                (= (.getYear date) (.getYear (:date %)))
                (= (.getMonth date) (.getMonth (:date %)))
                (= (.getDate date) (.getDate (:date %))))
          markers))


(defn init-main [_]
  [{:wefeedus
    {:position {}
     :zoom {}
     :hours {}
     :date {}
     :form
     {:select
      {:transforms
       (sorted-map-by <
                      :set-zoom [{msg/topic [:zoom]
                                  (msg/param :value) {}}]
                      :set-position [{msg/topic [:position]
                                      (msg/param :value) {}}]
                      :load-markers [{msg/topic [:markers]
                                      (msg/param :value) {}}])}}}}])

(defn init-add [_]
  [{:add
     {:meal
       {:transforms
        {:add-meal [{msg/topic [:add :meal]
                     (msg/param :type) {}
                     (msg/param :name) {}
                     (msg/param :start) {}
                     (msg/param :end) {}
                     (msg/param :date) {}}]}}}}])

(defn publish-marker [marker]
  (when marker [{msg/type :add-marker msg/topic [:add-marker] :value marker}]))

(def wefeedus-app
  {:version 2
   :transform [[:set-position [:position] set-value-transform]
               [:set-zoom [:zoom] set-value-transform]
               [:set-hours [:hours] set-value-transform]
               [:set-date [:date] set-value-transform]
               [:inc-date [:date] (fn [o m] (js/Date. (+ (.getTime o) (* 24 3600 1000))))]
               [:dec-date [:date] (fn [o m] (js/Date. (- (.getTime o) (* 24 3600 1000))))]
               [:load-markers [:markers] load-markers]

               [:add-meal [:add :meal] (fn [o m] (select-keys m #{:type :name
                                                                 :start :end
                                                                 :date}))]]
   :derive #{[{[:add :meal] :meal
               [:position] :position} [:add-marker]
               (fn [o {meal :meal pos :position}]
                 (if meal (merge meal pos) o)) :map]
             [{[:add-marker] :add
               [:markers] :markers} [:all-markers]
               (fn [_ {:keys [add markers]}]
                 (if add (conj markers add) markers)) :map]
             [{[:all-markers] :markers
               [:hours] :hours
               [:date] :date} [:visi-markers] visible-markers :map]}
   :effect #{[#{[:add-marker]} publish-marker :single-val]}
   :emit [{:init init-main}
          [#{[:position]
             [:zoom]
             [:hours]
             [:date]
             [:visi-markers]} (app/default-emitter [:wefeedus])]
          {:init init-add}
          [#{[:add :*]} (app/default-emitter [])]]
   :focus {:add [[:add] [:wefeedus]]
           :wefeedus  [[:wefeedus]]
           :default :add}})
