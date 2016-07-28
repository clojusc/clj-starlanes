(ns starlanes.finance.company-test
  (:require [clojure.test :refer :all]
            [starlanes.finance.company :as company]
            [starlanes.util :as util]))


(deftest test-get-new-company
  (let [result (company/get-new-company)]
    (is (= (result :name) "")))
  (let [result (company/get-new-company "A")]
    (is (= (result :name) "A"))))

(deftest test-add-company
  (is (= [{:name "Al"} {:name "Be"} {:name "Ca"}]
         (util/fake-game-data :companies)))
  (let [[company-name game-data] (company/add-company util/fake-game-data)]
    (is (= [{:name "Al"} {:name "Be"} {:name "Ca"} {:name "De"}]
           (game-data :companies)))
    (is (= ["Er"] (game-data :companies-queue)))
    (is (= "De" company-name))
    (let [[company-name game-data] (company/add-company game-data)]
      (is (= [{:name "Al"} {:name "Be"} {:name "Ca"} {:name "De"} {:name "Er"}]
             (game-data :companies)))
      (is (= [] (game-data :companies-queue)))
      (is (= "Er" company-name)))))

(deftest test-filter-company
  (let [companies [{:name "A"}
                   {:name "B"}]]
    (is (= [{:name "A"}
            {:name "B"}]
           (company/filter-company "a" companies)))
    (is (= [{:name "B"}]
           (company/filter-company "A" companies)))))

(deftest test-remove-company
  (let [game-data {:companies [{:name "Al"}
                               {:name "Be"}]
                   :companies-queue ["Ca" "De" "Er"]}
        game-data (company/remove-company "Al" game-data)]
    (is (= [{:name "Be"}] (game-data :companies)))
    (is (= ["Al" "Ca" "De" "Er"] (game-data :companies-queue)))))

(deftest test-remove-companies
  (let [game-data {:companies [{:name "Al"}
                               {:name "Be"}
                               {:name "Ca"}]
                   :companies-queue ["De" "Er"]}
        game-data (company/remove-companies ["Al" "Ca"] game-data)]
    (is (= [{:name "Be"}] (game-data :companies)))
    (is (= ["Al" "Ca" "De" "Er"] (game-data :companies-queue)))))

(deftest test-get-companies-base-counts
  (let [counts (company/get-companies-base-counts util/fake-game-data)]
    (is (= {"A" 3, "C" 1, "B" 2} counts))
    (is (= 3 (counts "A")))
    (is (= 2 (counts "B")))
    (is (= 1 (counts "C")))
    (is (= nil (counts "Z")))))

(deftest test-get-company-base-count
    (is (= 3 (company/get-company-base-count "A" util/fake-game-data)))
    (is (= 2 (company/get-company-base-count "B" util/fake-game-data)))
    (is (= 1 (company/get-company-base-count "C" util/fake-game-data)))
    (is (= 0 (company/get-company-base-count "Z" util/fake-game-data))))

(deftest test-get-company-star-count
  (is (= 3 (company/get-company-star-count "A" util/fake-game-data)))
  (is (= 1 (company/get-company-star-count "B" util/fake-game-data)))
  (is (= 0 (company/get-company-star-count "C" util/fake-game-data)))
  (is (= 0 (company/get-company-star-count "Z" util/fake-game-data))))

(deftest test-get-companies-star-counts
  (is (= {"A" 3, "B" 1}
         (company/get-companies-star-counts util/fake-game-data))))

(deftest test-get-share-value
  (is (= 1800 (company/get-share-value "A" util/fake-game-data)))
  (is (= 700 (company/get-share-value "B" util/fake-game-data)))
  (is (= 100 (company/get-share-value "C" util/fake-game-data)))
  (is (= 0 (company/get-share-value "Z" util/fake-game-data)))
  (is (= 0 (company/get-share-value "" util/fake-game-data)))
  (is (= 0 (company/get-share-value nil util/fake-game-data)))
  (testing "Edge cases"
    (let [fake-game-data {}]
      (is (= 0 (company/get-share-value "A" fake-game-data)))
      (is (= 0 (company/get-share-value "B" fake-game-data)))
      (is (= 0 (company/get-share-value "C" fake-game-data)))
      (is (= 0 (company/get-share-value "Z" fake-game-data)))
      (is (= 0 (company/get-share-value "" fake-game-data)))
      (is (= 0 (company/get-share-value nil fake-game-data))))))

(deftest test-get-share-values
  (is (= [1800 700 100] (company/get-share-values util/fake-game-data))))

(deftest test-get-share-values-with-company
  (is (= [[1800 "A"] [700 "B"] [100 "C"]]
         (company/get-share-values-with-company util/fake-game-data))))

(deftest test-get-share-values-with-company-names
  (is (= [["Altair Starways" 1800] ["Betelgeuse, Ltd." 700]
          ["Capella Cargo Co." 100]]
         (company/get-share-values-with-company-names util/fake-game-data))))

(deftest test-get-company-value
  (is (= 2880000 (company/get-company-value "A" util/fake-game-data)))
  (is (= 700000 (company/get-company-value "B" util/fake-game-data)))
  (is (= 60000 (company/get-company-value "C" util/fake-game-data))))

(deftest test-get-companies-values
  (is (= {:A 2880000, :B 700000, :C 60000}
         (company/get-companies-values util/fake-game-data)))
  (is (= {:A 2880000, :B 700000, :C 60000}
        (company/get-companies-values ["A" "B" "C"] util/fake-game-data))))

(deftest test-get-filtered-companies
  (is (= [[:a5 "C"] [:d1 "A"] [:e1 "A"] [:e2 "A"]]
         (company/get-filtered-companies ["A" "C"] util/fake-game-data)))
  (is (= [[:a3 "B"] [:a5 "C"] [:b3 "B"]]
         (company/get-filtered-companies ["B" "C"] util/fake-game-data)))
  (is (= [[:a3 "B"] [:b3 "B"]]
         (company/get-filtered-companies ["B"] util/fake-game-data))))

(deftest test-get-greatest-company
  (is (= "A" (company/get-greatest-company
              (util/get-companies-letters)
              util/fake-game-data)))
  (is (= "C" (company/get-greatest-company ["C"] util/fake-game-data)))
  (is (= "A" (company/get-greatest-company ["A" "C"] util/fake-game-data)))
  (is (= "B" (company/get-greatest-company ["B" "C"] util/fake-game-data)))
  (is (= "C" (company/get-greatest-company ["C" "D" "E"] util/fake-game-data)))
  (testing
    "This one needs to mock out the random function used to choose in the event
    of a tie."
    (with-redefs [rand-nth (fn [data] (first data))]
      (is
        (=
          "E"
          (company/get-greatest-company ["D" "E"] util/fake-game-data))))))

(deftest test-get-losers
  (is (= ["A" "B" "D"] (company/get-losers "C" ["A" "B" "C" "D"]))))

(deftest test-set-new-owner
  (let [star-map (util/fake-game-data :star-map)]
    (is (= "B" (star-map :a3)))
    (is (= "C" (star-map :a5)))
    (is (= "B" (star-map :b3)))
    (is (= "A" (star-map :d1)))
    (is (= "A" (star-map :e1)))
    (is (= "A" (star-map :e2))))
  (let [star-map ((company/set-new-owner
                    "A" "F" util/fake-game-data) :star-map)]
    (is (= "B" (star-map :a3)))
    (is (= "C" (star-map :a5)))
    (is (= "B" (star-map :b3)))
    (is (= "F" (star-map :d1)))
    (is (= "F" (star-map :e1)))
    (is (= "F" (star-map :e2)))))

(deftest test-set-new-owners
  (let [star-map (util/fake-game-data :star-map)]
    (is (= "B" (star-map :a3)))
    (is (= "C" (star-map :a5)))
    (is (= "B" (star-map :b3)))
    (is (= "A" (star-map :d1)))
    (is (= "A" (star-map :e1)))
    (is (= "A" (star-map :e2))))
  (let [star-map ((company/set-new-owners
                    ["A" "B" "C"]
                    "F"
                    util/fake-game-data) :star-map)]
    (is (= "F" (star-map :a3)))
    (is (= "F" (star-map :a5)))
    (is (= "F" (star-map :b3)))
    (is (= "F" (star-map :d1)))
    (is (= "F" (star-map :e1)))
    (is (= "F" (star-map :e2)))))

(deftest test-merge-companies
  (let [star-map (util/fake-game-data :star-map)]
    (is (= "B" (star-map :a3)))
    (is (= "C" (star-map :a5)))
    (is (= "B" (star-map :b3)))
    (is (= "A" (star-map :d1)))
    (is (= "A" (star-map :e1)))
    (is (= "A" (star-map :e2))))
  (let [game-data (company/-merge-companies
                    :a4
                    {:name "Alice"}
                    [[:a3 "B"] [:b3 "B"] [:a5 "C"]]
                    util/fake-game-data)
        star-map (game-data :star-map)]
    (is (= "B" (star-map :a3)))
    (is (= "B" (star-map :a5)))
    (is (= "B" (star-map :b3)))
    (is (= "A" (star-map :d1)))
    (is (= "A" (star-map :e1)))
    (is (= "A" (star-map :e2)))))


