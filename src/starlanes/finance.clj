(ns starlanes.finance
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
            [starlanes.finance.stock :as stock]
            [starlanes.finance.company :as company]
            [starlanes.player :as player]
            [starlanes.util :as util]))


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
  (let [shares (stock/get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company-name share] shares]
      (let [share-price (company/get-share-value
                          (str (first company-name)) game-data)
            value (* share share-price)]
        (display-company-data company-name share share-price value)))
    (util/display \newline)
    (display-companies-values game-data)
    (util/display \newline)
    (util/input const/continue-prompt)
    nil))

(defn display-players-earnings [game-data]
  )

