(ns compound.interest
  (:require
   [java-time.api :as time])
  (:import
   (java.math RoundingMode)))

(def Wallet
  "A wallet consists of:

   - `:m` Monthly instalment
   - `:p` Initial principle balance
   - `:r` Rate of interest (in decimal form)"
  [:map
   [:m decimal?]
   [:p decimal?]
   [:r decimal?]])

(defn round-half-even
  [b]
  (.setScale b 2 RoundingMode/ROUND_HALF_EVEN)

(defn grow
  [balance bigm bigr]
  {:pre [(decimal? balance) (decimal? bigm) (decimal? bigr)]}
  (.add (.add balance bigm)
        (.multiply balance bigr)))

(defn monthly
  ([balance account]
   (monthly balance account (* 50 12)))
  ([balance account months]
   (assert (decimal? balance))
   (let [{:keys [m r]} account
         bigm          (bigdec m)
         bigr          (bigdec r)
         start         (time/adjust (time/local-date) :first-day-of-next-month)
         interval      (time/months 1)]
     (take months
           (iterate (fn [m] (-> m
                               (update :balance #(grow % bigm bigr))
                               (update :month time/plus interval)))
                    {:balance balance
                     :month   start})))))
