(ns starlanes.finance
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
            [starlanes.finance.stock :as stock]
            [starlanes.finance.company :as company]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(defn display-player-earnings [player-name game-data]
  (let [shares (stock/get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company-name share] shares]
      (let [share-price (company/get-share-value
                          (str (first company-name)) game-data)
            value (* share share-price)]
        (util/display
          (str \tab company-name ": " value " ("share " shares @ " share-price
               " " const/currency-name "s each)" \newline))))
    (util/display \newline)
    (util/input const/continue-prompt)
    nil))

(defn display-companies-values [game-data]
  )

(defn display-players-earnings [game-data]
  )

