(ns starlanes.player-test
  (:require [clojure.test :refer :all]
            [starlanes.game :as game]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(deftest test-player-data-factory
  (let [player (player/player-data-factory)]
    (is (= (player :name) ""))
    (is (= (player :cash) 0.0))
    (is (= (player :stocks) nil))))

(deftest test-get-players-names
  (is (= ["Alice" "Bob" "Carol"]
         (sort (player/get-players-names util/fake-game-data)))))