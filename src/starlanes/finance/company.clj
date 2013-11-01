(ns starlanes.finance.company
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
            [starlanes.finance.stock :as stock]
            [starlanes.player :as player]
            [starlanes.util :as util]))


(defn company-factory []
  {:name ""})

(defn get-new-company
  ([]
    (company-factory))
  ([name]
    (assoc
      (company-factory)
      :name name)))

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

(defn remove-companies [companies-names game-data]
  (let [company-name (first companies-names)
        remaining (rest companies-names)]
    (cond
      (not (nil? company-name))
        (remove-companies
          remaining
          (remove-company company-name game-data))
      :else game-data)))

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
    (stock/add-player-shares
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
        total-company-shares (stock/get-company-shares
                               company-letter game-data)]
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
                  (= val (ffirst sorted)) [key val]))
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
        game-data (remove-companies
                    (map util/get-company-name losers)
                    game-data)]
    ; XXX recalculate value of winning company, with map updated
    ; XXX if the stock is over the threshold, perform a split
    (game-map/update-coords keyword-coord winner game-data)))

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
