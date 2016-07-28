(ns starlanes.layout-test
  (:require [clojure.test :refer :all]
            [starlanes.game :as game]
            [starlanes.layout :as layout]
            [starlanes.util :as util]))


(deftest test-get-header
  (let [result (layout/get-header)
        expected "a | b | c | d | e"]
    (is (= result expected))))

(deftest test-get-row-header
  (let [result (layout/get-row-header "10")
        expected "10 | "]
    (is (= result expected))))

(deftest test-get-row-header-buffer
  (let [result (layout/get-row-header-buffer)
        expected 4]
    (is (= result expected))))

(deftest test-check-item
  (is (= :star (layout/get-item-name "*")))
  (is (= :empty (layout/get-item-name ".")))
  (is (= :outpost (layout/get-item-name "+")))
  (is (= :company (layout/get-item-name "A")))
  (is (= :company (layout/get-item-name "E"))))

(deftest test-colorize-item
  (is (= "\33[0;33m*\33[m" (layout/colorize-item "*")))
  (is (= "\33[1;30m.\33[m" (layout/colorize-item ".")))
  (is (= "\33[1;32m+\33[m" (layout/colorize-item "+")))
  (is (= "\33[1;36mA\33[m" (layout/colorize-item "A")))
  (is (= "\33[1;36mE\33[m" (layout/colorize-item "E"))))

(deftest test-get-row-string
  (let [game-data (game/create-star-map-for-game (game/game-data-factory))
        grouped-data (group-by layout/grouper (game-data :star-map))
        row-data (sort (get grouped-data "3"))
        result (layout/get-row-string row-data)]
    (is (= row-data [[:a3 "."] [:b3 "."] [:c3 "*"] [:d3 "."] [:e3 "."]]))
    (is (= result (str "\33[1;30m.\33[m "
                       "  \33[1;30m.\33[m "
                       "  \33[0;33m*\33[m "
                       "  \33[1;30m.\33[m "
                       "  \33[1;30m.\33[m")))))

(deftest test-get-row
  (let [game-data (game/create-star-map-for-game (game/game-data-factory))
        grouped-data (group-by layout/grouper (game-data :star-map))
        result (layout/get-row "1" grouped-data)]
    (is (= result (str "\33[1;30m.\33[m "
                       "  \33[1;30m.\33[m "
                       "  \33[1;30m.\33[m "
                       "  \33[1;30m.\33[m "
                       "  \33[1;30m.\33[m")))))

(deftest test-grouper
  (let [game-data (game/create-star-map-for-game (game/game-data-factory))
        star-map (game-data :star-map)]
    (is (= (layout/grouper [:a10 "*"]) "10"))
    (is (= ((group-by layout/grouper star-map) "1")
           [[:a1 "."] [:b1 "."] [:c1 "."] [:d1 "."] [:e1 "."]]))))

(deftest test-keyword-grouper
  (let [game-data (game/create-star-map-for-game (game/game-data-factory))
        star-map (game-data :star-map)]
    (is (= (layout/keyword-grouper [:a10 "*"]) "a"))
    (is (= ((group-by layout/keyword-grouper star-map) "a")
           [[:a1 "."] [:a2 "."] [:a3 "."] [:a4 "."] [:a5 "."]]))))
