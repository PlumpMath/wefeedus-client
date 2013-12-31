(ns wefeedus-client.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [wefeedus-client.behavior :as behavior]
            [wefeedus-client.rendering :as rendering]
            [wefeedus-client.services :as services]))

;; In this namespace, the application is built and started.

(defn create-app [render-config]
  (let [app (app/build behavior/wefeedus-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        app-model (render/consume-app-model app render-fn)
        now (js/Date.)
        hour (.getHours now)]
    (app/begin app)

    (.getCurrentPosition
     (.. js/window -navigator -geolocation)
     (fn [pos] (let [coords (.-coords pos)
                    lon (.-longitude coords)
                    lat (.-latitude coords)]
                (.log js/console "You are at " lon "-" lat)
                (p/put-message (:input app) {msg/type :set-position msg/topic [:position]
                                             :value {:lon lon :lat lat}})))
     #(.log js/console "Could not fetch your geo position."))

    (p/put-message (:input app) {msg/type :set-zoom msg/topic [:zoom]
                                 :value 1})
    #_(p/put-message (:input app) {msg/type :load-markers msg/topic [:markers]
                                 :markers #{{:lon 8.485 :lat 49.455 :type :soup
                                             :date now :start 1000 :end 1200}
                                            {:lon 8.491 :lat 49.451 :type :meal
                                             :date now :start 480 :end 1000}
                                            {:lon 8.494 :lat 49.458 :type :cake
                                             :date now :start 1200 :end 1320}}})
    (p/put-message (:input app) {msg/type :set-date msg/topic [:date]
                                 :value now})
    (p/put-message (:input app) {msg/type :set-hours msg/topic [:hours]
                                 :value [(* (max 8 hour) 60)
                                         (* (min 22 (+ 2 (max 8 hour))) 60)]})

    {:app app :app-model app-model}))

(defn ^:export main []
  (let [app (create-app (rendering/render-config))
        chans (services/client-connect! "localhost:9123")
        services (services/->Services (:app app) chans)]
    (app/consume-effects (:app app) (partial services/services-fn chans))
    (p/start services)
    app))
