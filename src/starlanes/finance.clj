(ns starlanes.finance
  (:require [starlanes.const :as const]
            [starlanes.game.movement :as game-move]
            [starlanes.finance.stock :as finance-stock]
            [starlanes.finance.company :as finance-company]
            [starlanes.util :as util]))


(defn get-new-bank [players-names]
  (into {}
        (map
          #(vector % {:cash 0.0})
          players-names)))

(defn get-bank [game-data]
  (game-data :bank))

(defn get-player-cash [player-name game-data]
  (get-in game-data [:bank player-name :cash] 0))

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
    (str \newline "Company valuations:\n\n"))
  (let [companies-letters (util/get-companies-letters game-data)
        values (finance-company/get-companies-values companies-letters game-data)]
    (doseq [[company-keyword total-value] values]
      (let [company-name (util/get-company-name company-keyword)]
        (util/display
          (str \tab company-name ": " total-value \newline)))))
  nil)

(defn display-companies-values [game-data]
  (util/clear-screen)
  (-display-companies-values game-data)
  (util/display \newline)
  (util/display \newline)
  (util/input const/continue-prompt)
  nil)

(defn -display-player-earnings [player-name game-data]
  (util/display
    (str "Here is your balance for cash-on-hand:" \newline \newline
         \tab (get-player-cash player-name game-data) \newline))
  (let [shares (finance-stock/get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company-name share] shares]
      (let [share-price (finance-company/get-share-value
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

(defn -display-players-earnings [];[game-data]
  nil)

(defn display-players-earnings [];[game-data]
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
  (let [shares (finance-stock/get-player-shares-with-companies player-name game-data)
        values (finance-company/get-share-values-with-company game-data)]
    (* const/dividend-multiplier
       (reduce +
               (map
                 #(* (second %1) (first %2))
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
   (let [company-letter (second
                          (first
                            (sort
                              (finance-company/get-share-values-with-company
                                game-data))))]
      (affordable? company-letter cash player-name game-data)))
  ([company-letter cash player-name game-data]
   (let [share-value (finance-company/get-share-value company-letter game-data)
         _do_something_with_player_name player-name]
     (if (<= share-value cash)
       true
       false))))

(defn display-stock-purchase-option [game-data];[company-letter game-data]
  (util/clear-screen)
  (util/display (str "Stock Exchange\n\n"))
  ; display prompt with current cash and finance-company/share info
  ; check to see if there are enough funds
  ; ensure aount is positive
  ; make purchase - update stock exchange, update bank
  (util/input const/continue-prompt)
  game-data)

(defn display-stock-purchase-options
  ([game-data]
    (display-stock-purchase-options
      ;;(util/get-companies-letters game-data)
      game-data))
  ([companies-letters game-data]
    (let [company-letter (first companies-letters)
          remaining (rest companies-letters)]
      (cond
        (not (nil? company-letter))
          (display-stock-purchase-options
            remaining
            (display-stock-purchase-option
              ;company-letter
              game-data))
        :else game-data))))

(defn let-player-purchase-stocks [game-data]
  (let [player-name (game-move/get-current-player game-data)
        ;; shares-data (finance-company/get-share-values game-data)
        ]
  (cond
    (affordable? player-name game-data)
      (display-stock-purchase-options game-data)
    :else game-data)))



