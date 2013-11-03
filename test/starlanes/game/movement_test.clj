(ns starlanes.game.movement-test
  (:require [clojure.test :refer :all]
            [starlanes.game.movement :as game]
            [starlanes.util :as util]))

(def initial-game-state
  {:total-moves 0
   :players [{:name "Alice"}]
   :player-order [0]})

(deftest test-get-remaining-moves
  (is (= 14 (game/get-remaining-moves util/fake-game-data))))

(deftest test-get-current-move-index
  (is (= 0 (game/get-current-move-index initial-game-state))))

(deftest test-get-current-player-index
  (is (= 0 (game/get-current-player-index initial-game-state))))

(deftest test-get-current-player
  (is (= {:name "Alice"} (game/get-current-player initial-game-state))))

(deftest test-moves-remain-many
  (is (= 14 (game/-moves-remain? util/fake-game-data))))

(deftest test-moves-remain-some
  (let [max-moves 10
        moves-so-far 4
        remaining 4]
    (let [win-by-turns false]
      (is (= 4 (game/-moves-remain?
                 win-by-turns max-moves moves-so-far remaining))))
    (let [win-by-turns true]
      (is (= 6 (game/-moves-remain?
                 win-by-turns max-moves moves-so-far remaining))))))

(deftest test-moves-remain-few
  (let [max-moves 10
        moves-so-far 8
        remaining 2]
    (let [win-by-turns false]
      (is (= 2 (game/-moves-remain?
                  win-by-turns max-moves moves-so-far remaining))))
    (let [win-by-turns true]
      (is (= 2 (game/-moves-remain?
                  win-by-turns max-moves moves-so-far remaining))))))

(deftest test-moves-remain-none
  (let [max-moves 6
        moves-so-far 6
        remaining 32]
    (let [win-by-turns false]
      (is (= 32 (game/-moves-remain?
                  win-by-turns max-moves moves-so-far remaining)))
      (is (= 0 (game/-moves-remain?
                  win-by-turns max-moves moves-so-far 0))))
    (let [win-by-turns true]
      (is (= 0 (game/-moves-remain?
                  win-by-turns max-moves moves-so-far remaining))))))

(deftest test-legal?
  (is (= true (game/legal? ["a" "b" "c"] "a")))
  (is (= false (game/legal? ["a" "b" "c"] "d"))))

