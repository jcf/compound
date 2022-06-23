(ns compound.repl
  (:require
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl :refer [set-init]]
   [compound]
   [ring.adapter.jetty :as jetty]))

(defrecord App [script-path handler]
  component/Lifecycle
  (start [this]
    (assoc this :handler (compound/make-handler
                          {:bootstrap-script (-> script-path io/resource slurp)})))
  (stop [this]
    (assoc this :handler nil)))

(defrecord Server [jetty port app]
  component/Lifecycle
  (start [this]
    (assoc this :jetty (jetty/run-jetty (:handler app) {:join? false :port port})))
  (stop [this]
    (.stop jetty)
    (assoc this :jetty nil)))

(defn config
  []
  {:app    {:script-path "compound/bootstrap.js"}
   :server {:port 3000}})

(defn system
  []
  (component/system-using
   (component/system-map
    :app    (map->App {})
    :server (map->Server {}))
   {:server [:app]}))

(set-init (fn [_system] (merge-with merge (system) (config))))
