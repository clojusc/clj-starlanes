(ns starlanes.player-test
  (:require [clojure.test :refer :all]
            [starlanes.game :as game]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(deftest test-player-data-factory
  (let [player (player/player-data-factory)]
    (is (= (player :name) ""))
    (is (= (player :cash) 0.0))))

(deftest test-get-cash
  (is (= 0.0 (player/get-cash (player/player-data-factory)))))

(deftest test-add-cash
  (is (= {:name "", :cash 10.0}
         (player/add-cash (player/player-data-factory) 10))))

(deftest test-remove-cash
  (is (= {:name "", :cash -20.0}
         (player/remove-cash (player/player-data-factory) 20))))

(deftest test-get-players-names
  (is (= ["Alice" "Bob" "Carol"]
         (sort (player/get-players-names util/fake-game-data)))))
