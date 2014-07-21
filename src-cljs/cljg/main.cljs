(ns cljg.main
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.walk :refer [keywordize-keys]]
            [goog.events :as events]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

;; -----------------------------------------------------------------------------
;; Keyboard
;; -----------------------------------------------------------------------------
(def keyboard-controls
  (atom {}))

(defn register-key-controls [shortcut-handlers]
  (swap! keyboard-controls
         (fn [current] (merge current shortcut-handlers))))

(defn register-key-control [shortcut handler]
  (js/console.log (str "Registering keyCode " shortcut))
  (swap! keyboard-controls
         (fn [current] (assoc current shortcut handler))))

(defn handle-key-down [e]
  ; 37 - <=
  ; 39 - =>
  ; 74 - j
  ; 75 - k
  (let [key-code (.-keyCode e)
        handler (get @keyboard-controls key-code)]
    (js/console.log (str "Pressed " key-code))
    (when (fn? handler) (handler e))))

(events/listen js/window (.-KEYDOWN events/EventType) handle-key-down)


;; -----------------------------------------------------------------------------
;; App
;; -----------------------------------------------------------------------------
(def app-state
  (atom
    {:img-num 0
     :images [{:src "http://d3dsacqprgcsqh.cloudfront.net/photo/aozB15g_460s.jpg"}
              {:src "http://d3dsacqprgcsqh.cloudfront.net/photo/aozB15g_460s.jpg"}
              {:src "http://d3dsacqprgcsqh.cloudfront.net/photo/aozB15g_460s.jpg"}
              {:src "http://d3dsacqprgcsqh.cloudfront.net/photo/agyMMgr_460s.jpg"}]}))

(defn image-view [image owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        {:className "clearfix"}
        (dom/h2 nil (:alt image))
        (dom/img #js {:className "img-responsive pull-right"
                      :width "500px"
                      :src (:src image)}
                 nil)))))

(defn handle-images-response [response app]
  (let [new-images (map #(keywordize-keys %) (get response "images"))
        next-url (get response "next-url")]
    (om/transact! app :images (fn [old-images]
                                (if (< (count old-images) 8)
                                  new-images
                                  (concat old-images new-images))))
    (om/transact! app :next-url (fn [_] next-url))))

(defn request-images
  ([success-handler]
   (request-images success-handler {}))
  ([success-handler params]
   (GET "/images.json"
        {:params params
         :handler success-handler
         :format :json})))

(defn set-img-index [app owner ix]
  (let [max-ix (dec (count (:images @app)))
        cur-ix (:img-num @app)
        new-ix (min (max 0 ix) max-ix)]
    (when (not= new-ix cur-ix)
      (js/console.log (str "Changing ix to " new-ix))
      (when (= new-ix max-ix)
        (request-images #(handle-images-response % app)
                        {:next (:next-url @app)}))
      (om/transact! app :img-num #(identity new-ix)))))

(defn image-stream [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:img-num 0})

    om/IWillMount
    (will-mount [_]
      (let [next-img #(set-img-index app owner (inc (:img-num @app)))
            prev-img #(set-img-index app owner (dec (:img-num @app)))]
        (register-key-controls {37 prev-img ; left
                                39 next-img ; right
                                74 next-img ; j
                                75 prev-img ; k
                                })
        (request-images #(handle-images-response % app))))

    om/IRenderState
    (render-state [this state]
      (let [ix (:img-num app)
            max-ix (dec (count (:images app)))
            current-image (nth (:images app) ix)]
        (dom/div
          #js {:className "row" :onKeyDown #(prn %)}
          (dom/div
            #js {:className "col-sm-8 text-right"}
            (dom/div nil (om/build image-view current-image)))
          (dom/div
            #js {:className "col-sm-4"}
            (dom/h2 nil "nine")
            (dom/p nil
                   (dom/button #js {:className "btn btn-default"
                                    :onClick #(set-img-index app owner (dec ix))}
                               "Prev")
                   " "
                   (dom/button #js {:className "btn btn-default"
                                    :onClick #(set-img-index app owner (inc ix))}
                               "Next"))

            (dom/p nil (dom/span #js {:className "label label-default"}
                                 (str "Gag " ix)))
            (dom/div
              nil
              (dom/p nil
                     (dom/kbd nil "k") " "
                     (dom/kbd nil "←")
                     " Previous")
              (dom/p nil
                     (dom/kbd nil "j") " "
                     (dom/kbd nil "→")
                     " Next"))))))))

(om/root
  image-stream
  app-state
  {:target (. js/document (getElementById "app"))})
