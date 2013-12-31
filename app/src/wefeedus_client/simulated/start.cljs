(ns wefeedus-client.simulated.start
  (:require [io.pedestal.app.render.push.handlers.automatic :as d]
            [wefeedus-client.start :as start]
            [wefeedus-client.rendering :as rendering]
            [wefeedus-client.simulated.services :as services]
            [goog.Uri]
            ;; This needs to be included somewhere in order for the
            ;; tools to work.
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app-tools.tooling :as tooling]))

(defn param [name]
  (let [uri (goog.Uri. (.toString  (.-location js/document)))]
    (.getParameterValue uri name)))

(defn ^:export main []
  (let [app (start/create-app (if (= "auto" (param "renderer"))
                                d/data-renderer-config
                                (rendering/render-config)))
        service (services/->MockServices (:app app))]
    (p/start service)
    app))
