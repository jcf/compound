(ns compound.domain
  (:require
   [clojure.spec.alpha :as s]))

(s/def :page/title string?)

(s/def :compound/page
  (s/keys :req [:page/body-attrs
                :page/html-attrs]
          :opt [:page/title]))

(s/def :compound.next/bootstrap-script string?)

(s/def :compound/config
  (s/keys :req-un [:compound.next/bootstrap-script]))
