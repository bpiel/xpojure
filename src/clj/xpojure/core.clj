(ns xpojure.core
  (:require [xpojure.handler :as handler]
            [xpojure.config :refer [env]]
            [xpojure.state :as state]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [xpojure.env :refer [defaults]]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defonce ^{:doc "The HTTP Server"} server (atom nil))

(def http-port-default 3000)

(defn server-config
  "Retrieve jetty adapter configuration"
  [http-port]
  {:pre [(integer? http-port)]}
  {:port                 http-port
   :join?                false
   :response-header-size 100000})

(defn start-server!
  "Start the HTTP listener with HANDLER-VAR on port HTTP-PORT."
  [handler-var http-port]
  (log/info "Starting HTTP listener...")
  (reset! server (jetty/run-jetty handler-var
                                  (server-config http-port)))
  (log/info (format "Started HTTP listener on %d" http-port)))

(defn stop-server!
  "Stop the HTTP listener."
  []
  (.stop @server))

(defn -main [& {http-port ":port" :as args}]
  (state/start-watcher)
  (start-server! #'handler/app (or http-port
                                   http-port-default)))


#_ (
    (mount/defstate ^{:on-reload :noop}
      http-server
      :start
      (http/start
       (-> env
           (assoc :handler (handler/app))
           (update :port #(or (-> env :options :port) %))))
      :stop
      (http/stop http-server))

    (mount/defstate ^{:on-reload :noop}
      repl-server
      :start
      (when-let [nrepl-port (env :nrepl-port)]
        (repl/start {:port nrepl-port}))
      :stop
      (when repl-server
        (repl/stop repl-server)))

    (defn stop-app []
      (doseq [component (:stopped (mount/stop))]
        (log/info component "stopped"))
      (shutdown-agents))

    (defn start-app [args]
      (doseq [component (-> args
                            (parse-opts cli-options)
                            mount/start-with-args
                            :started)]
        (log/info component "started"))
      (logger/init (:log-config env))
      ((:init defaults))
      (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

    (defn -main [& args]
      (start-app args)))
