(ns compound.interest-test
  (:require
   [compound.interest :as sut]
   [clojure.test :refer [are deftest is]]
   [matcher-combinators.clj-test]))

(def ^:private account
  {:m 100
   :p 1000
   :r 0.01})

(deftest round-half-even
  (are [in out] (= out (sut/round-half-even in))
    (bigdec 0)      (bigdec 0)
    (bigdec 11)     (bigdec 11)
    (bigdec 0.123)  (bigdec 0.12)
    (bigdec 1.123)  (bigdec 1.12)
    (bigdec 11.123) (bigdec 11.12)))

(deftest monthly
  (let [months            (sut/monthly (bigdec 1000) account)
        {:keys [balance]} (last months)]
    (is (= 600 (count months)))
    (is (instance? BigDecimal balance))
    (is (= 4254769.67M (sut/round-half-even balance)))))
