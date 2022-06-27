(ns compound.dev
  (:require
   [com.stuartsierra.component :as component]
   [compound.repl :as repl]))

(defn -main
  []
  (let [config (assoc-in (repl/config) [:server :port] 8080)]
    (component/start-system
     (merge-with merge (repl/system) config))
    @(promise)))
