(ns starlanes.finance-test
  (:require [clojure.test :refer :all]
            [starlanes.finance :as finance]
            [starlanes.util :as util]))


(deftest test-get-new-bank
  (is (= {"A" {:cash 0.0}, "B" {:cash 0.0}, "C" {:cash 0.0}}
         (finance/get-new-bank ["A" "B" "C"]))))

(deftest test-get-bank
  (is (= util/fake-bank-data (finance/get-bank util/fake-game-data))))

(deftest test-player-get-cash
  (is (= 200 (finance/get-player-cash "Alice" util/fake-game-data)))
  (is (= 260 (finance/get-player-cash "Bob" util/fake-game-data)))
  (is (= 570 (finance/get-player-cash "Carol" util/fake-game-data))))

(deftest test-player-add-cash
  (is (= 210
         (finance/get-player-cash
           "Alice"
           (finance/add-player-cash "Alice" 10 util/fake-game-data)))))

(deftest test-player-remove-cash
  (is (= 550
         (finance/get-player-cash
           "Carol"
           (finance/remove-player-cash "Carol" 20 util/fake-game-data)))))

(deftest test-get-dividends
  (is (= 1800.0
         (finance/get-dividends "Alice" util/fake-game-data)))
  (is (= 950.0
         (finance/get-dividends "Bob" util/fake-game-data)))
  (is (= 890.0
         (finance/get-dividends "Carol" util/fake-game-data))))
