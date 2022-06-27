(ns compound.test.http
  (:require
   [compound.next :as next]
   [ring.middleware.anti-forgery.strategy :as anti-forgery]))

(defn static-token-strategy
  [static-token]
  (reify anti-forgery/Strategy
    (valid-token? [_ _request token]
      (= static-token token))
    (get-token [_ _request]
      static-token)
    (write-token [_ _request response _token]
      response)))

(defn replace-anti-forgery-strategy
  [config strategy]
  (assoc-in config [::next/site-defaults :security :anti-forgery :strategy] strategy))
