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

(defn display-companies-values [game-data]
  (util/display
    (str \newline "Company valuations:" \newline \newline))
  (let [companies-letters (util/get-companies-letters game-data)
        values (company/get-companies-values companies-letters game-data)]
    (doseq [[company-keyword total-value] values]
      (let [company-name (util/get-company-name company-keyword)]
        (util/display
          (str \tab company-name ": " total-value \newline))))))

(defn display-player-earnings [player-name game-data]
  (util/clear-screen)
  (util/display
    (str "Here is your balance for cash-on-hand:" \newline \newline
         (get-player-cash player-name game-data) \newline))
  (let [shares (stock/get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company-name share] shares]
      (let [share-price (company/get-share-value
                          (str (first company-name)) game-data)
            value (* share share-price)]
        (display-company-data company-name share share-price value)))
    (display-companies-values game-data)
    (util/display \newline)
    (util/display \newline)
    (util/input const/continue-prompt)
    nil))

(defn display-players-earnings [game-data]
  )

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
