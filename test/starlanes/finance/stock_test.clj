(ns starlanes.finance.stock-test
  (:require [clojure.test :refer :all]
            [starlanes.finance.stock :as stock]
            [starlanes.util :as util]))


(deftest test-get-new-exchange
  (is (= [:A]
         (sort
           (keys
             (stock/get-new-stock-exchange ["A"])))))
  (is (= [:A :B]
         (sort
           (keys
             (stock/get-new-stock-exchange ["A" "B"])))))
  (is (= [:A :B :C]
         (sort
           (keys
             (stock/get-new-stock-exchange ["A" "B" "C"])))))
  (is (= ["Alice" "Bob"]
         (sort
           (keys
             ((stock/get-new-stock-exchange ["A"] ["Alice" "Bob"]) :A)))))
  (is (= ["Alice" "Bob" "Carol"]
         (sort
          (keys
            ((stock/get-new-stock-exchange
              ["A" "B"]
              ["Alice" "Bob" "Carol"]) :B)))))
  (is (= ["Alice" "Bob" "Carol" "Dave"]
         (sort
          (keys
            ((stock/get-new-stock-exchange
              ["A" "B" "C"]
              ["Alice" "Bob" "Carol" "Dave"]) :C))))))

(deftest test-get-stock-exchange
  (is (= [:A :B :C]
         (sort
           (keys
             (stock/get-stock-exchange util/fake-game-data))))))

(deftest test-get-company-holdings
  (is (= ["Alice" "Bob" "Carol"]
         (sort
           (keys
             (stock/get-company-holdings "A" util/fake-game-data)))))
  (is (= ["Carol"]
         (sort
           (keys
             (stock/get-company-holdings "B" util/fake-game-data)))))
  (is (= ["Bob" "Carol"]
         (sort
           (keys
             (stock/get-company-holdings "C" util/fake-game-data))))))

(deftest test-get-company-shares
  (is (= 1600 (stock/get-company-shares "A" util/fake-game-data)))
  (is (= 1000 (stock/get-company-shares "B" util/fake-game-data)))
  (is (= 600 (stock/get-company-shares "C" util/fake-game-data)))
  )

(deftest test-get-player-shares
  (is (= 1000 (stock/get-player-shares "A" "Alice" util/fake-game-data)))
  (is (= 500 (stock/get-player-shares "A" "Bob" util/fake-game-data)))
  (is (= 100 (stock/get-player-shares "A" "Carol" util/fake-game-data)))
  (is (= 0 (stock/get-player-shares "B" "Alice" util/fake-game-data)))
  (is (= 1000 (stock/get-player-shares "B" "Carol" util/fake-game-data)))
  (is (= 500 (stock/get-player-shares "C" "Bob" util/fake-game-data)))
  (is (= 100 (stock/get-player-shares "C" "Carol" util/fake-game-data)))
  (is (= 0 (stock/get-player-shares "Z" "Bob" util/fake-game-data))))

(deftest test-get-player-shares-with-company
  (is (= [:A 100]
         (stock/get-player-shares-with-company
           "A" "Carol" util/fake-game-data)))
  (is (= [:B 1000]
         (stock/get-player-shares-with-company
           "B" "Carol" util/fake-game-data)))
  (is (= [:C 100]
         (stock/get-player-shares-with-company
           "C" "Carol" util/fake-game-data))))

(deftest test-get-player-shares-with-companies
  (is (= [[:A 100] [:B 1000] [:C 100]]
         (stock/get-player-shares-with-companies
           "Carol" util/fake-game-data)))
  (is (= [[:A 100] [:B 1000] [:C 100]]
         (stock/get-player-shares-with-companies
           ["A" "B" "C"] "Carol" util/fake-game-data))))

(deftest test-get-players-shares-with-player
  (is (= ["Carol" {:A 100, :B 1000, :C 100}]
         (stock/get-players-shares-with-player
           ["A" "B" "C"] "Carol" util/fake-game-data))))

(deftest test-get-players-shares
  (is (= {"Alice" {:A 1000, :B 0, :C 0, :D 0, :E 0},
          "Bob" {:A 500, :B 0, :C 500, :D 0, :E 0},
          "Carol" {:A 100, :B 1000, :C 100, :D 0, :E 0}}
         (stock/get-players-shares util/fake-game-data)))
  (is (= {"Alice" {:A 1000, :B 0, :C 0},
          "Bob" {:A 500, :B 0, :C 500},
          "Carol" {:A 100, :B 1000, :C 100}}
         (stock/get-players-shares ["A" "B" "C"] util/fake-game-data))))

(deftest test-add-player-shares
  (is (= 1000 (stock/get-player-shares "A" "Alice" util/fake-game-data)))
  (let [game-data (stock/add-player-shares
                    "A" "Alice" 1000 util/fake-game-data)]
    (is (= 2000 (stock/get-player-shares "A" "Alice" game-data)))))

(deftest test-compute-value
  (let [assets [1000 [{:stock 12 :value 23.50} {:stock 100 :value 50}]]]
    (is (= 6282.0 (stock/compute-value assets)))
    (is (= 6282.0 (stock/compute-value (first assets) (second assets))))))
