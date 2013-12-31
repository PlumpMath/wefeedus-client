(ns wefeedus-client.rendering
  (:require [goog.ui.TwoThumbSlider]
            [goog.ui.SliderBase]
            [goog.ui.DatePicker]
            [goog.ui.Component]
            [domina :as dom]
            [domina.events :as domev]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers.automatic :as d])
  (:require-macros [wefeedus-client.html-templates :as html-templates]))

;; Load templates.
(def templates (html-templates/wefeedus-client-templates))


(defn- date-string
  "Pretty print js date object."
  [date]
  (let [day (.getDate date)
        mon (inc (.getMonth date))
        year (rem (.getYear date) 100)]
    (str (if (< day 10) 0) day
         "." (if (< mon 10) 0) mon
         "." (if (< year 10) 0) year)))



(defn render-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer
                                     path
                                     (:wefeedus templates))
        _ (dom/append! (dom/by-id parent) (html {:id id}))
        geo-map (ol.Map. (clj->js {:target "map"
                                   :layers [(ol.layer.Tile.
                                             (clj->js {:source (ol.source.OSM.)}))]
                                   :renderer ol.RendererHint.CANVAS
                                   :view (ol.View2D.
                                          (clj->js {:center (trans 0 0)
                                                    :zoom 4}))}))]
    (render/set-data! renderer path {:geo-map geo-map})
    (domev/listen! (dom/by-id :add-button)
                   :click
                   #(p/put-message transmitter {msg/type :set-focus
                                                msg/topic msg/app-model
                                                :name :add}))))


(defn- resize-slider []
  (let [new-width (- (.-clientWidth (dom/by-id :header))
                     (.-clientWidth (dom/by-id :date-stepper))
                     (.-clientWidth (dom/by-id :add-button))
                     10)]
    (.log js/console "setting width" new-width)
    (dom/set-style! (dom/by-id :hour-slider) :width (str new-width "px"))))

(defn render-date-button [renderer [_ path] transmitter]
  (let [btn (dom/by-id :date-stepper)]
    (domev/listen! btn :click
                   (fn [e]
                     (let [middle (+ (.-offsetLeft btn)
                                     (/ (.-clientWidth btn) 2))]
                       (p/put-message transmitter
                                      {msg/type (if (> (:clientX e) middle)
                                                  :inc-date
                                                  :dec-date)
                                       msg/topic [:date]}))))))


(defn render-date [renderer [_ path old new] transmitter]
  (dom/set-text! (dom/by-id :date-stepper) (date-string new)))

(defn render-hours [renderer [_ path _ [start end]] transmitter]
  (let [slider (:slider (render/get-data renderer path))]
    (.setValueAndExtent slider start (- end start))))

(defn- print-hour [mins]
  (let [r (rem mins 60)]
    (str (int (/ mins 60)) ":" (if (< r 10) 0) r)))



(defn render-hour-slider [renderer [_ path] transmitter]
  (let [slider (goog.ui.TwoThumbSlider.)]
    (render/set-data! renderer path {:slider slider})
    (doto slider
      (.setMinimum (* 8 60))
      (.setMaximum (* 22 60))
      (.setMinExtent 30)
      (.decorate (dom/by-id :hour-slider))
      (domev/listen! goog.ui.Component.EventType.CHANGE
                     #(let [v (.getValue slider)
                            e (.getExtent slider)]
                        (dom/set-text! (dom/by-id :start-hour) (print-hour v))
                        (dom/set-text! (dom/by-id :end-hour) (print-hour (+ v e)))))
      (domev/listen! goog.ui.SliderBase.EventType.DRAG_END
                     #(let [v (.getValue slider)
                            e (.getExtent slider)]
                        (p/put-message transmitter {msg/type :set-hours
                                                    msg/topic [:hours]
                                                    :value [v (+ e v)]}))))))


(defn- trans [lon lat]
  (ol.proj.transform (clj->js [lon lat]) "EPSG:4326" "EPSG:3857"))


(defn render-position [renderer [_ path _ pos] transmitter]
  (-> (:geo-map (render/get-data renderer (butlast path)))
      (.getView)
      (.setCenter (trans (:lon pos) (:lat pos)))))


(defn render-markers [renderer [_ path old new] transmitter]
  (doseq [t [:soup :meal :cake]]
    (render-markers-type t renderer
                         [_ path old (filter #(= t (:type %)) new)]
                         transmitter)))


(defn render-markers-type [marker-type renderer [_ path old new] transmitter]
  (let [feats (map #(ol.Feature.
                     (clj->js {:name (:name %)
                               :geometry
                               (ol.geom.Point. (trans (:lon %) (:lat %)))}))
                   new)
        map-obj (:geo-map (render/get-data renderer (butlast path)))
        old-vector (get (render/get-data renderer path) marker-type)
        new-vector (ol.layer.Vector.
                    (clj->js
                     {:source (ol.source.Vector. (clj->js {:features feats}))
                      :style
                      (ol.style.Style.
                       (clj->js
                        {:symbolizers
                         [(ol.style.Icon.
                           (clj->js {:url (str (name marker-type) ".png"),
                                     :yOffset -22}))]}))}))]
    (when old-vector
      (.removeLayer map-obj old-vector))
    (render/set-data! renderer path (assoc (render/get-data renderer path)
                                      marker-type new-vector))
    (.addLayer map-obj new-vector)))


(defn add-meal-dialog [renderer [_ path] input-queue]
  (let [html (templates/add-template renderer
                                     path
                                     (:add templates))
        parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        date-picker (goog.ui.DatePicker.)]
    (dom/append! (dom/by-id parent) (html))
    (.decorate date-picker (dom/by-id :add-date))
    (domev/listen! (dom/by-id :add-meal-button)
                   :click
                   (fn [e]
                     (p/put-message input-queue {msg/type :add-meal
                                                 msg/topic [:add :meal]
                                                 :type (keyword (dom/value (dom/by-id :add-type)))
                                                 :name (dom/value (dom/by-id :add-name))
                                                 :start (int (dom/value (dom/by-id :add-start)))
                                                 :end (int (dom/value (dom/by-id :add-end)))
                                                 :date (js/Date. (.getTime (.getDate date-picker)))})
                     (p/put-message input-queue {msg/type :set-focus
                                                 msg/topic msg/app-model
                                                 :name :wefeedus})))
    (domev/listen! (dom/by-id :cancel-meal-button)
                   :click
                   (fn [e]
                     (p/put-message input-queue {msg/type :set-focus
                                                 msg/topic msg/app-model
                                                 :name :wefeedus})))
    (domev/listen! (dom/by-id :add-dialog-close)
                   :click
                   (fn [e]
                     (p/put-message input-queue {msg/type :set-focus
                                                 msg/topic msg/app-model
                                                 :name :wefeedus})))))


(defn render-config []
  [[:node-create  [:wefeedus] render-page]
   [:node-create  [:wefeedus :date] render-date-button]
   [:node-create  [:wefeedus :hours] render-hour-slider]

   [:value [:wefeedus :position] render-position]
   [:value [:wefeedus :visi-markers] render-markers]
   [:value [:wefeedus :date] render-date]
   [:value [:wefeedus :hours] render-hours]

   [:node-destroy   [:wefeedus] d/default-exit]

   ;; add-dialog
   [:node-create [:add] add-meal-dialog]
   [:node-destroy [:add] #(dom/destroy! (dom/by-id :add-dialog))]])
