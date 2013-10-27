(ns starlanes.finance
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
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

(defn company-factory []
  {:name ""})

(defn get-new-company
  ([]
    (company-factory))
  ([name]
    (assoc
      (company-factory)
      :name name)))

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

(defn get-next-company [game-data]
  (let [current-count (count (game-data :companies))
        next-index (inc current-count)]))

(defn add-company [game-data]
  (let [available (game-data :companies-queue)
        company-name (first available)
        game-data (conj game-data {:companies-queue (rest available)})
        company (get-new-company company-name)
        companies (concat (game-data :companies) [company])]
    [company-name (conj game-data {:companies companies})]))

(defn filter-company
  "Reomve company data whose names match the passed name."
  [company-name companies]
  (filter
    (fn [x]
      (not= (x :name) company-name))
    companies))

(defn remove-company [company-name game-data]
  (let [companies-queue (sort
                          (concat
                            (game-data :companies-queue) [company-name]))
        companies (filter-company company-name (game-data :companies))]
    (conj game-data {:companies companies :companies-queue companies-queue})))

(defn announce-new-company [company-name]
  (util/beep)
  (util/display
    (str
      "A new shipping company has been formed!" \newline
      "It's name is '" company-name "'." \newline)))

(defn announce-player-bonus [current-player company-name units mod]
  (util/display
    (str
      \newline
      (current-player :name) ", you have been awarded "
      const/founding-shares " share(s) in " \newline
      company-name ", currently valued at " \newline
      (* units mod) " "
      const/currency-name "s each." \newline \newline))
  (util/input const/continue-prompt))

(defn make-announcements [current-player company-name units mod]
  (announce-new-company company-name)
  (announce-player-bonus current-player company-name units mod))

(defn create-company
  "Creating a company should only ever happen if all attempts have been made
  to identify possibly merges, first. As such, there should never be another
  company in the immediate neighborhood (adjoining spaces) of a new company;
  there should only be stars and/or outposts."
  [keyword-coord current-player share-modifier game-data]
  (let [outpost-coords (map first (game-map/get-neighbor-outposts
                                    keyword-coord game-data))
        units (count outpost-coords)
        [company-name game-data] (add-company game-data)
        item-char (str (first company-name))]
    (make-announcements
      current-player company-name (inc units) share-modifier)
    (add-player-shares
      item-char
      (current-player :name)
      const/founding-shares
      (game-map/multi-update-coords
        (concat outpost-coords [keyword-coord])
        item-char
        game-data))))

(defn create-star-company
  "Update the game data with a new company created from adjacent outposts."
  [keyword-coord current-player game-data]
  (create-company
    keyword-coord current-player const/share-modifier-star game-data))

(defn create-outpost-company
  "Update the game data with a new company created from adjacent outposts."
  [keyword-coord current-player game-data]
  (create-company
    keyword-coord current-player const/share-modifier-base game-data))

(defn get-companies-base-counts
  "For each company, count the number of pieces (bases) they have on the board."
  [game-data]
  (let [company-letters (map second (game-map/get-companies-data game-data))]
    (util/count-occurances company-letters)))

(defn get-company-base-count
  ""
  [company-letter game-data]
  (let [star-count ((get-companies-base-counts game-data) company-letter)]
    (if (nil? star-count)
      0
      star-count)))

(defn get-companies-star-counts
  "Get all companies that are next to stars and the number of stars they are
  next to."
  [game-data]
  (let [stars (game-map/get-star-coords game-data)
        star-neighbors (map
                         #(game-map/get-neighbor-companies % game-data)
                         stars)]
    (util/count-occurances
      (take-nth 2
        (rest
          (flatten
            (remove empty? star-neighbors)))))))

(defn get-company-star-count
  "For the commpany letter passed, count the number of company bases that are
  adjacent to a star."
  [company-letter game-data]
  (let [star-count ((get-companies-star-counts game-data) company-letter)]
    (if (nil? star-count)
      0
      star-count)))

(defn get-share-value
  ""
  [company-letter game-data]
  (let [star-count (get-company-star-count company-letter game-data)
        base-count (get-company-base-count company-letter game-data)]
    (+
      (* const/share-modifier-star star-count)
      (* const/share-modifier-base base-count))))

(defn get-company-value
  "Things that affect company value:
    * total number of shares held by all players
    * value of shares

  Value of shares is affected by:
    * number of company pieces on the board
    * number of company pieces on the board adjacent to stars"
  [company-letter game-data]
  (let [share-value (get-share-value company-letter game-data)
        total-company-shares (get-company-shares company-letter game-data)]
    (* total-company-shares share-value)))

(defn get-companies-values
  "For each company in the game, get its value and return a hash map with this
  data."
  ([game-data]
   (get-companies-values (util/get-companies-letters) game-data))
  ([companies-letters game-data]
    (into
      {}
      (map
        (fn [x] {(keyword x) (get-company-value x game-data)})
        companies-letters))))

(defn get-filtered-companies
  "Get the company data from the game state for just the companies whose first-
  letter abbreviation is provided."
  [companies-letters game-data]
  (filter
    (fn [x] (util/in? companies-letters (second x)))
    (game-map/get-companies-data game-data)))

(defn get-greatest-company
  "Get all company values, and identify those with the greatest value. In the
  event of a tie, randomly select from the top-valued companies."
  [companies-letters game-data]
  (let [data (get-companies-values companies-letters game-data)
        sorted (reverse
                 (sort
                   (map
                     (fn [x] [(val x) (key x)])
                     data)))]
    (name
      (first
        (rand-nth
          (remove
            empty?
            (map
              (fn [[val key]]
                (if
                  (= val (first (first sorted))) [key val]))
              sorted)))))))

(defn get-losers
  ""
  [winner all-companies]
  (sets/difference
    (set all-companies)
    (set [(name winner)])))

(defn set-new-owner
  ""
  [old-company new-company game-data]
  (let [coords (game-map/get-item-coords old-company game-data)]
    (game-map/multi-update-coords coords new-company game-data)))

(defn set-new-owners
  ""
  [old-companies new-company game-data]
  (let [old-company (first old-companies)
        remaining (rest old-companies)]
    (cond
      (not (nil? old-company))
        (set-new-owners
          remaining
          new-company
          (set-new-owner old-company new-company game-data))
      :else game-data)))

(defn -merge-companies
  "Merge the companies, given the following data:

     * the keyword coordinate for the current move (e.g., :e1)
     * data for the current player (e.g., {:name \"Carol\"})
     * coordinate data for the companies under question (e.g.,
      [[:d1 \"A\"] [:b1 \"B\"]])
     * game state
  "
  [keyword-coord current-player companies-coords game-data]
  (let [distinct-companies (distinct (map second companies-coords))
        winner (get-greatest-company distinct-companies game-data)
        losers (get-losers winner distinct-companies)
        game-data (set-new-owners losers winner game-data)]
    ; XXX recalculate value of winning company, with map updated
    ; XXX if the stock is over the threshold, perform a split
    game-data))

(defn merge-companies
  [keyword-coord current-player companies-coords game-data]
  (util/display (str \newline "Merging companies ..." \newline))
  (let [game-data (-merge-companies keyword-coord
                                    current-player
                                    companies-coords
                                    game-data)]
    (util/input const/continue-prompt)
    game-data))

(defn expand-company
  ""
  [keyword-coord current-player company-item-data game-data]
  (let [company-letter (second company-item-data)
        outpost-coords (map first (game-map/get-neighbor-outposts
                                    keyword-coord game-data))]
    ; XXX this is an insufficient final solution; see the following issue for
    ; more details:
    ;   https://github.com/oubiwann/clj-starlanes/issues/6
    ; we're going to want to create a function that takes a list of neighbor
    ; outposts, converts them to the company, and then recurses on all those
    ; outposts' neighbors that are outposts, performing the same action
    ;
    ; So probably the best way to do this is to have a function whose
    ; responsibility it is to
    (game-map/multi-update-coords
      (concat outpost-coords [keyword-coord])
      company-letter
      game-data)))

(defn get-named-shares [player-name game-data]
  (map
    (fn [[x y]] [(util/get-company-name x) y])
    (get-player-shares-with-companies player-name game-data)))

(defn display-player-earnings [player-name game-data]
  (let [shares (get-named-shares player-name game-data)]
    (util/display
      (str \newline "Here are your current earnings: " \newline \newline))
    (doseq [[company share] shares]
      (let [share-price (get-share-value (str (first company)) game-data)
            value (* share share-price)]
        (util/display
          (str \tab company ": " value " ("share " shares @" share-price
               " " const/currency-name "s each)" \newline))))
    (util/display \newline)
    (util/input const/continue-prompt)
    nil))

(defn display-companies-values [game-data]
  )

(defn display-players-earnings [game-data]
  )

