(ns starlanes.finance-test
  (:require [clojure.test :refer :all]
            [starlanes.finance :as finance]
            [starlanes.util :as util]))


(deftest test-get-new-bank
  (is (= {"A" {:cash 0.0}, "B" {:cash 0.0}, "C" {:cash 0.0}}
         (finance/get-new-bank ["A" "B" "C"]))))

(deftest test-get-bank
  (is (= util/fake-bank-data (finance/get-bank util/fake-game-data))))

(deftest test-get-dividends
  (is (= 1800.0
         (finance/get-dividends "Alice" util/fake-game-data)))
  (is (= 950.0
         (finance/get-dividends "Bob" util/fake-game-data)))
  (is (= 890.0
         (finance/get-dividends "Carol" util/fake-game-data))))
