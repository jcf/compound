(ns compound.html
  (:require
   [clojure.java.shell :refer [sh]]
   [clojure.pprint :refer [pprint]]
   [hickory.core :as hickory]))

(defn convert
  [s]
  (mapv hickory/as-hiccup (hickory/parse-fragment s)))

(defn pprint-str
  ^String [obj]
  (with-out-str (pprint obj)))

(comment
  (spit "/tmp/markup.edn" (-> "pbpaste" sh :out convert pprint-str)))
