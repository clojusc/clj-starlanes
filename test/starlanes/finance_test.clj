(ns starlanes.finance-test
  (:require [clojure.test :refer :all]
            [starlanes.finance :as finance]
            [starlanes.game.movement :as game-move]
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
  (testing "Test the single-parameter option"
    (with-redefs [game-move/get-current-player (fn [x] {:name "Alice"})]
      (is (= false (finance/affordable? util/fake-game-data))))
    (with-redefs [game-move/get-current-player (fn [x] {:name "Carol"})]
      (is (= true (finance/affordable? util/fake-game-data)))))
  (testing "Test the double-parameter option"
    (is (= false (finance/affordable? "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? "Bob" util/fake-game-data)))
    (is (= true (finance/affordable? "Carol" util/fake-game-data))))
  (testing "Test with cash passed as a parameter"
    (is (= false (finance/affordable? 0 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? 99 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? 100 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? 1000 "Alice" util/fake-game-data))))
  (testing "Test with cash passed as a parameter"
    (is (= false (finance/affordable? "A" 100 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "A" 500 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "A" 1000 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? "A" 1800 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "B" 100 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "B" 200 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "B" 500 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? "B" 700 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? "C" 100 "Alice" util/fake-game-data))))
  (testing "Let's take a look at some edge-cases"
    (is (= true (finance/affordable? "" 100 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? nil 100 "Alice" util/fake-game-data)))
    (is (= false (finance/affordable? "A" 0 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? "" 0 "Alice" util/fake-game-data)))
    (is (= true (finance/affordable? nil 0 "Alice" util/fake-game-data)))
    (let [fake-game-data {:companies []
                           :players [{:name "Alice"}]
                           :stock-exchange  {:A {"Alice" {}}}}]
      (is (= true (finance/affordable? "" 100 "Alice" fake-game-data)))
      (is (= true (finance/affordable? nil 100 "Alice" fake-game-data)))
      (is (= true (finance/affordable? "A" 0 "Alice" fake-game-data)))
      (is (= true (finance/affordable? "" 0 "Alice" fake-game-data)))
      (is (= true (finance/affordable? nil 0 "Alice" fake-game-data))))))
