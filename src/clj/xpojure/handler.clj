(ns xpojure.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [xpojure.layout :refer [error-page]]
            [xpojure.routes.home :refer [home-routes]]
            [compojure.route :as route]
            [xpojure.middleware :as middleware]))

(def app-routes
  (routes
   (-> #'home-routes
       (wrap-routes middleware/wrap-formats))
   (route/not-found
    (:body
     (error-page {:status 404
                  :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
