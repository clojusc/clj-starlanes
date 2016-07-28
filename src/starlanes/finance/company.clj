(ns starlanes.finance.company
  (:require [clojure.set :as sets]
            [starlanes.const :as const]
            [starlanes.game.map :as game-map]
            [starlanes.finance.stock :as stock]
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

(defn get-names
  "Extract the total company names from the game data."
  [game-data]
  (->> game-data
       :companies-queue
       (map (fn [x] {:name x}))
       (into (:companies game-data))))

(defn match-name-letter
  "Given a letter and a company name, this function will take the first letter
  of the company name and check that it matches the given letter. Success
  returns the company name, failure returns an empty string."
  [match-letter company-name]
  (if (util/starts-with? company-name match-letter)
      company-name))

(defn company-data->name
  ""
  [letter company-data]
  (->> company-data
       :name
       (match-name-letter letter)))

(defn get-name
  "Given a letter representing a company, find the corresponding company name."
  [letter game-data]
  (->> game-data
       (get-names)
       (map #(company-data->name letter %))
       (filter string?)
       (first)))

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

(defn remove-company [company-letter game-data]
  (let [company-name (get-name company-letter game-data)
        companies-queue (sort
                          (concat
                            (game-data :companies-queue) [company-name]))
        companies (filter-company company-name (game-data :companies))]
    (conj game-data {:companies companies :companies-queue companies-queue})))

(defn remove-companies [companies-letters game-data]
  (let [[company-letter & remaining] companies-letters]
    (cond
      (not (nil? company-letter))
        (remove-companies
          remaining
          (remove-company company-letter game-data))
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
      const/founding-shares " share(s) in \n"
      company-name ", currently valued at \n"
      (* units mod) " "
      const/currency-name "s each.\n\n"))
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
  (->> game-data
       (game-map/get-star-coords)
       (map #(game-map/get-neighbor-companies % game-data))
       (remove empty?)
       (flatten)
       (rest)
       (take-nth 2)
       (util/count-occurances)))

(defn get-company-star-count
  "For the commpany letter passed, count the number of company bases that are
  adjacent to a star."
  [company-letter game-data]
  (let [star-count ((get-companies-star-counts game-data) company-letter)]
    (if (nil? star-count)
      0
      star-count)))

(defn -get-share-value
  ""
  [company-letter game-data]
  (let [star-count (get-company-star-count company-letter game-data)
        base-count (get-company-base-count company-letter game-data)]
    (+
      (* const/share-modifier-star star-count)
      (* const/share-modifier-base base-count))))

(defn get-share-value
  ""
  [company-letter game-data]
  (cond
    (empty? company-letter)
      0
    :else (-get-share-value company-letter game-data)))

(defn get-share-values
  ""
  [game-data]
  (map
    #(get-share-value % game-data)
    (util/get-companies-letters game-data)))

(defn get-share-values-with-company
  ""
  [game-data]
  (map
    #(vector
      (get-share-value % game-data)
      %)
    (util/get-companies-letters game-data)))

(defn get-share-values-with-company-names
  ""
  [game-data]
  (map
    #(vector
      (util/get-company-name (keyword %))
      (get-share-value % game-data))
    (util/get-companies-letters game-data)))

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
   (get-companies-values (util/get-companies-letters game-data) game-data))
  ([companies-letters game-data]
    (->> companies-letters
         (map (fn [x] {(keyword x) (get-company-value x game-data)}))
         (into {}))))

(defn get-filtered-companies
  "Get the company data from the game state for just the companies whose first-
  letter abbreviation is provided."
  [companies-letters game-data]
  (filter
    (fn [x] (util/in? companies-letters (second x)))
    (game-map/get-companies-data game-data)))

(defn get-sorted-companies-values
  ""
  [companies-letters game-data]
  (->> game-data
       (get-companies-values companies-letters)
       (map (fn [x] [(val x) (key x)]))
       (sort)
       (reverse)))

(defn get-greatest-company
  "Get all company values, and identify those with the greatest value. In the
  event of a tie, randomly select from the top-valued companies."
  [companies-letters game-data]
  (let [sorted (get-sorted-companies-values companies-letters game-data)]
    (->> sorted
         (map (fn [[val key]]
                (if (= val (ffirst sorted)) [key val])))
         (remove empty?)
         (rand-nth)
         (first)
         (name))))

(defn get-cheapest-company []
  ""
  )

(defn get-losers
  ""
  [winner all-companies]
  (->> (set [(name winner)])
       (sets/difference (set all-companies))
       (into [])
       (sort)))

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
    * coordinate data for the companies under question (e.g.,
      [[:d1 \"A\"] [:b1 \"B\"]])
    * game state"
  [keyword-coord companies-coords game-data]
  (let [distinct-companies (distinct (map second companies-coords))
        winner (get-greatest-company distinct-companies game-data)
        losers (get-losers winner distinct-companies)]
    ;; XXX recalculate value of winning company, with map updated
    ;; XXX if the stock is over the threshold, perform a split
    (->> game-data
         (remove-companies losers)
         (game-map/update-coords keyword-coord winner)
         (set-new-owners losers winner))))

(defn merge-companies
  [keyword-coord companies-coords game-data]
  (util/display "\nMerging companies ...\n")
  (util/input const/continue-prompt)
  (-merge-companies keyword-coord companies-coords game-data))

(defn expand-company
  ""
  [keyword-coord company-item-data game-data]
  (let [company-letter (second company-item-data)
        outpost-coords (map first (game-map/get-neighbor-outposts
                                    keyword-coord game-data))]
    ; XXX this is an insufficient final solution; see the following issue for
    ; more details:
    ;   https://github.com/clojusc/clj-starlanes/issues/6
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
