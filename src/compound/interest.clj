(ns compound.interest
  (:require
   [java-time :as time])
  (:import
   (java.math RoundingMode MathContext)))

(defn round-half-even
  [b]
  (.setScale b 2 RoundingMode/HALF_EVEN))

(defn monthly
  ([balance account]
   (monthly balance account (* 50 12)))
  ([balance account months]
   (assert (instance? BigDecimal balance))
   (let [{:keys [r]} account
         bigr        (bigdec r)
         start       (time/adjust (time/local-date) :first-day-of-next-month)
         interval    (time/months 1)]
     (take months
           (iterate (fn [m] (-> m
                               (update :balance #(.add % (.multiply % bigr)))
                               (update :month time/plus interval)))
                    {:balance balance
                     :month   start})))))
