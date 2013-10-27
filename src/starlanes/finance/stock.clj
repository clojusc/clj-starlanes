(ns starlanes.finance.stock
  (:require [starlanes.const :as const]
            [starlanes.finance.stock :as stock]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(defn get-new-stock-exchange
  ([companies-letters]
    (util/get-map-of-maps (map keyword companies-letters)))
  ([companies-letters players-names]
    (let [players-holdings (util/get-map-of-maps players-names)]
      (util/get-map-of-maps (map keyword companies-letters) players-holdings))))

(defn get-stock-exchange [game-data]
  (game-data :stock-exchange))

(defn get-company-holdings
  ""
  [company-letter game-data]
  ((get-stock-exchange game-data) (keyword company-letter)))

(defn get-company-shares
  ""
  [company-letter game-data]
  (reduce +
          (map
            (fn [x] (x :shares))
            (vals
              (get-company-holdings company-letter game-data)))))

(defn get-player-shares
  ""
  [company-letter player-name game-data]
  (let [company-holdings (get-company-holdings company-letter game-data)]
    (if company-holdings
      (let [player-holdings (company-holdings player-name)]
        (if player-holdings
          (let [player-shares (player-holdings :shares)]
            (if player-shares
              player-shares
              0))
          0))
      0)))

(defn get-player-shares-with-company [company-letter player-name game-data]
  [(keyword company-letter)
   (get-player-shares
     company-letter player-name game-data)])

(defn get-player-shares-with-companies
  ([player-name game-data]
   (get-player-shares-with-companies
     (util/get-companies-letters game-data)
     player-name
     game-data))
  ([companies-letters player-name game-data]
    (map
      #(get-player-shares-with-company % player-name game-data)
      companies-letters)))

(defn get-named-shares [player-name game-data]
  (map
    (fn [[x y]] [(util/get-company-name x) y])
    (get-player-shares-with-companies player-name game-data)))

(defn get-players-shares-with-player
  [companies-letters player-name game-data]
  [player-name
   (into {}
         (get-player-shares-with-companies
           companies-letters
           player-name
           game-data))])

(defn get-players-shares
  ""
  ([game-data]
    (get-players-shares (util/get-companies-letters) game-data))
  ([companies-letters game-data]
    (into {}
          (map
            #(get-players-shares-with-player companies-letters % game-data)
            (player/get-players-names game-data)))))

(defn add-player-shares
  [company-letter player-name new-shares game-data]
  (let [company-keyword (keyword company-letter)
        exchange-data (get-stock-exchange game-data)
        company-data (exchange-data company-keyword)
        player-data (company-data player-name)
        old-shares (get-player-shares company-letter player-name game-data)
        player-data (conj player-data {:shares (+ new-shares old-shares)})
        company-data (conj company-data {player-name player-data})
        exchange-data (conj exchange-data {company-keyword company-data})]
    (conj game-data {:stock-exchange exchange-data})))

(defn compute-stock-value [{stock :stock value :value}]
  (* stock value))

(defn compute-stocks-value [stocks-data]
  (reduce + (map compute-stock-value stocks-data)))

(defn compute-value
  "The assets parameter is a map which has the following structure:
    {:cash <float> :stock <integer> :value <float>}
  where :value is stock price of the associated stock."
  ([assets]
    (apply compute-value assets))
  ([cash stocks-data]
    (+ cash (compute-stocks-value stocks-data))))
