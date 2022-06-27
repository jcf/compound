(ns compound.next-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [compound.next :as sut]
   [compound.test.http :as test.http]
   [matcher-combinators.clj-test]
   [ring.mock.request :as mock]))

(def ^:private config
  {:bootstrap-script "console.log('Compound');"})

(def handler
  (sut/make-handler {::sut/site-defaults {:security {:anti-forgery false}}}))

;;; ----------------------------------------------------------------------------
;;; GET /

(deftest home-response
  (let [handler (sut/make-handler config)]
    (is (match?
         {:status  200
          :headers {"Content-Type" "text/html; charset=utf-8"}}
         (handler (mock/request :get "/"))))))

;;; ----------------------------------------------------------------------------
;;; POST /calc

(deftest calc-response
  (testing "with a missing anti-forgery token"
    (let [handler (sut/make-handler config)]
      (is (match?
           {:status  403
            :headers {"Content-Type" "text/html; charset=utf-8"}
            :body    "<h1>Invalid anti-forgery token</h1>"}
           (handler (mock/request :post "/calc"))))))

  (testing "with a valid anti-forgery token"
    (let [token   "token"
          handler (sut/make-handler (test.http/replace-anti-forgery-strategy
                                     config
                                     (test.http/static-token-strategy token)))]
      (is (match?
           {:status  200
            :headers {"Content-Type" "text/html; charset=utf-8"}}
           (handler (-> (mock/request :post "/calc")
                        (mock/body {"__anti-forgery-token" token
                                    "p"                    "1000"
                                    "m"                    "100"
                                    "r"                    "0.01"}))))))))
