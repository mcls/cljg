(ns cljg.handler
  (:use [hiccup.core])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [cljg.ninegag :as ninegag]))

;; ----------------------------------------------------------------------------
;; Page layout
;; ----------------------------------------------------------------------------

(defn stylesheet [path]
  [:link {:href path :rel "stylesheet"}])

(defn js-file [path]
  [:script {:src path}])

(defn home-page []
  (html
    [:head
     [:title "nine"]
     (stylesheet "/bower_components/bootstrap/dist/css/bootstrap.min.css")
     (stylesheet "/bower_components/bootstrap/dist/css/bootstrap-theme.min.css")
     (js-file "http://fb.me/react-0.11.0.js")]
    [:body
     [:div.container [:div#app "Loading..."]]
     (js-file "/js/js.js")]))


;; ----------------------------------------------------------------------------
;; Routes
;; ----------------------------------------------------------------------------

(defroutes app-routes
  (GET "/" []
       (home-page))
  (GET "/images.json" {params :params}
       {:status 200
        :headers {"Content-Type" "application/json; charset=utf-8"}
        :body (json/write-str (ninegag/parse-page (:next params)))})
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
