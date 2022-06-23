(ns compound.interest-test
  (:require
   [compound.interest :as sut]
   [clojure.test :refer [are deftest is]]
   [matcher-combinators.clj-test]))

(deftest round-half-even
  (are [in out] (= out (sut/round-half-even in))
    (bigdec 0)      (bigdec 0)
    (bigdec 11)     (bigdec 11)
    (bigdec 0.123)  (bigdec 0.12)
    (bigdec 1.123)  (bigdec 1.12)
    (bigdec 11.123) (bigdec 11.12)))

(deftest monthly
  (let [months (sut/monthly (bigdec 1000) {:r 0.01})
        {:keys [balance]} (last months)]
    (is (= 600 (count months)))
    (is (instance? BigDecimal balance))
    (is (= 387706.33M (sut/round-half-even balance)))))
