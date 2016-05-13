(ns xpojure.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [xpojure.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[xpojure started successfully using the development profile]=-"))
   :middleware wrap-dev})
