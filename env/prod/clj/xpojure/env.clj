(ns xpojure.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[xpojure started successfully]=-"))
   :middleware identity})
