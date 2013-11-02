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
  (is (= 50 (finance/get-player-cash "Alice" util/fake-game-data)))
  (is (= 100 (finance/get-player-cash "Bob" util/fake-game-data)))
  (is (= 570 (finance/get-player-cash "Carol" util/fake-game-data))))

(deftest test-player-add-cash
  (is (= 60
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

(deftest test-affordable?
  ; XXX add test with with-redefs on get-current-player
  (is (= false (finance/affordable? "Alice" util/fake-game-data)))
  (is (= true (finance/affordable? "Bob" util/fake-game-data)))
  (is (= true (finance/affordable? "Carol" util/fake-game-data)))
  ; XXX add test with cash values as added parameter
  ; XXX add test with company letters as added parameter
  )
