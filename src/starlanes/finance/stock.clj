(ns starlanes.finance.stock
  (:require [starlanes.const :as const]
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
            (fn [x] (or (:shares x) 0))
            (vals
              (get-company-holdings company-letter game-data)))))

(defn get-player-shares
  ""
  ([player-name game-data]
   (map
    #(get-player-shares % player-name game-data)
    (util/get-companies-letters)))
  ([company-letter player-name game-data]
  (get-in
    game-data
    [:stock-exchange (keyword company-letter) player-name :shares]
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
  (update-in
    game-data
    [:stock-exchange (keyword company-letter) player-name :shares]
    (fnil + 0) new-shares))

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

