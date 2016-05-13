(ns xpojure.dev-middleware
  (:require [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-error-page
      wrap-exceptions))
