(ns user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [com.stuartsierra.component.user-helpers :refer [set-dev-ns]]))

(set-refresh-dirs "dev" "src" "test")
(s/check-asserts true)

(set-dev-ns 'compound.repl)
