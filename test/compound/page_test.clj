(ns compound.page-test
  (:require
   [clojure.test :refer [deftest is]]
   [compound.page :as sut]
   [hickory.core :as hickory]
   [hickory.select :as sel]
   [matcher-combinators.clj-test]))

(deftest layout
  (let [html-attrs {:class "foo" :lang "en-GB"}
        body-attrs {:class "bar"}
        page       {:page/html-attrs html-attrs
                    :page/body-attrs body-attrs}
        content    [:h1 "Coming soon!"]
        doc        (-> (sut/layout page content)
                       sut/html
                       hickory/parse
                       hickory/as-hickory)
        html-tag   (first (sel/select (sel/tag :html) doc))
        body-tag   (first (sel/select (sel/tag :body) doc))]
    (is (match? {:attrs html-attrs} html-tag))
    (is (match? {:attrs body-attrs} body-tag))))
