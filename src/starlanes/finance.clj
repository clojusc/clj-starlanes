(ns starlanes.finance
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
            [starlanes.game.movement :as game-move]
            [starlanes.finance.stock :as stock]
            [starlanes.finance.company :as company]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(defn get-new-bank [players-names]
  (into {}
        (map
          #(vector % {:cash 0.0})
          players-names)))

(defn get-bank [game-data]
  (game-data :bank))

(defn get-player-cash [player-name game-data]
  (get-in game-data [:bank player-name :cash]))

(defn add-player-cash [player-name amount game-data]
  (update-in game-data [:bank player-name :cash] + amount))

(defn remove-player-cash [player-name amount game-data]
  (update-in game-data [:bank player-name :cash] - amount))

(defn display-company-data [company-name shares share-price share-value]
  (util/display
    (str \tab company-name ": " share-value " (" shares " shares @ "
         share-price " " const/currency-name "s each)" \newline)))

(defn -display-companies-values [game-data]
  (util/display
    (str \newline "Company valuations:" \newline \newline))
  (let [companies-letters (util/get-companies-letters game-data)
        values (company/get-companies-values companies-letters game-data)]
    (doseq [[company-keyword total-value] values]
      (let [company-name (util/get-company-name company-keyword)]
        (util/display
          (str \tab company-name ": " total-value \newline)))))
  nil)

(defn display-companies-values [game-data]
  (util/clear-screen)
  (-display-companies-values)
  (util/display \newline)
  (util/display \newline)
  (util/input const/continue-prompt)
  nil)

(defn -display-player-earnings [player-name game-data]
  (util/display
    (str "Here is your balance for cash-on-hand:" \newline \newline
         \tab (get-player-cash player-name game-data) \newline))
  (let [shares (stock/get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company-name share] shares]
      (let [share-price (company/get-share-value
                          (str (first company-name)) game-data)
            value (* share share-price)]
        (display-company-data company-name share share-price value)))
    nil))

(defn display-player-earnings [player-name game-data]
  (util/clear-screen)
  (-display-player-earnings player-name game-data)
  (-display-companies-values game-data)
  (util/display \newline)
  (util/display \newline)
  (util/input const/continue-prompt)
  nil)

(defn -display-players-earnings [game-data]
  nil)

(defn display-players-earnings [game-data]
  nil)

(defn display-endgame-stats [game-data]
  (util/clear-screen)
  (-display-players-earnings game-data)
  (-display-companies-values game-data)
  (util/display \newline)
  (util/display \newline)
  ; XXX get top-score
  ; XXX determine tie-breaking, if necessary
  ; XXX display "scoreboard"
  nil)

(defn get-dividends [player-name game-data]
  (let [shares (stock/get-player-shares-with-companies player-name game-data)
        values (company/get-share-values game-data)]
    (* const/dividend-percentage
       (reduce +
               (map
                 #(* (second %1) %2)
                 shares
                 values)))))

(defn pay-dividends [game-data]
  (let [player-name ((game-move/get-current-player game-data) :name)
        dividends (get-dividends player-name game-data)]
    (add-player-cash player-name dividends game-data)))

(defn affordable?
  ""
  ([game-data]
   (affordable? ((game-move/get-current-player game-data) :name) game-data))
  ([player-name game-data]
   (affordable? (get-player-cash player-name game-data) player-name game-data))
  ([cash player-name game-data]
    ; XXX the following is not optimal; find a faster and clearner way
    (let [company-letter (second (first
      (sort
        (company/get-share-values-with-company game-data))))]
      (affordable? company-letter cash player-name game-data)))
  ([company-letter cash player-name game-data]
   (let [share-value (company/get-share-value company-letter game-data)]
     (cond
       (<= share-value cash) true
       :else false))))

(defn display-stock-purchase [company-letter game-data]
  ; display intro/heading
  ; display prompt with current cash and company/share info
  ; check to see if there are enough funds
  ; ensure aount is positive
  ; make purchase - update stock exchange, update bank
  ; return new game data
  )

(defn display-stock-purchasing-options [companies-letters game-data]
  (let [company-letter (first companies-letters)
        remaining (rest companies-letters)]
    (cond
      (not (nil? company-letter))
        (display-stock-purchasing-options
          remaining
          (display-stock-purchase company-letter game-data))
      :else game-data)))

(defn let-player-purchase-stocks [game-data]
  (let [player-name (game-move/get-current-player)
        shares-data (company/get-share-values game-data)]
  (cond
    (affordable? player-name game-data)
      (display-stock-purchasing-options game-data)
    :else game-data)))



