(ns starlanes.const-test
  (:require [clojure.test :refer :all]
            [starlanes.const :as const]))


(deftest test-xgrid
  (let [result const/xgrid
        expected ["a" "b" "c" "d" "e"]]
    (is (= result expected))))

(deftest test-ygrid
  (let [result const/ygrid
        expected ["1" "2" "3" "4" "5"]]
    (is (= result expected))))

(deftest test-make-y-coord
  (is (= (const/make-y-coord 1) "1"))
  (is (= (const/make-y-coord 10) "10")))

(deftest test-grid-coord-values
  (is (= const/xgrid-start 97))
  (is (= const/xgrid-end 102))
  (is (= const/ygrid-start 1))
  (is (= const/ygrid-end 6)))
