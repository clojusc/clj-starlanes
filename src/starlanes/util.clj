(ns starlanes.util
  (:require [clojure.string :as string]
            [clojure.set :refer [intersection]]
            [starlanes.const :as const]))


(def fake-star-map
  {:a1 "*", :a2 ".", :a3 "B", :a4 ".", :a5 "C",
   :b1 ".", :b2 ".", :b3 "B", :b4 ".", :b5 ".",
   :c1 ".", :c2 ".", :c3 "*", :c4 ".", :c5 "+",
   :d1 "A", :d2 "*", :d3 ".", :d4 ".", :d5 ".",
   :e1 "A", :e2 "A", :e3 ".", :e4 "*", :e5 "."})

(def fake-bank-data
  {"Alice" {:cash 50}
   "Bob" {:cash 100}
   "Carol" {:cash 570}})

(def fake-exchange-data
  {:A
    {"Alice" {:shares 1000}
     "Bob" {:shares 500}
     "Carol" {:shares 100}}
   :B
    {"Carol" {:shares 1000}}
   :C
    {"Bob" {:shares 500}
     "Carol" {:shares 100}}})

(def fake-game-data
  {:star-map fake-star-map,
   :total-moves 0,
   :players [
    {:name "Alice"},
    {:name "Bob"},
    {:name "Carol"}],
   :player-order [1 0],
   :move 0,
   :companies [{:name "Al"}
               {:name "Be"}
               {:name "Ca"}],
   :companies-queue ["De" "Er"],
   :bank fake-bank-data,
   :stock-exchange fake-exchange-data,
   :rand nil})

(defn display [data]
  (.print (System/out) data))

(defn exit []
  (display (str \newline "Shutting down game ... " \newline))
  (System/exit 0))

(defn clear-screen []
  (display "\u001b[2J")
  (display "\u001B[0;0f"))

(defn beep []
  (clear-screen)
  (display (char 7))
  (clear-screen))

(defn input [prompt]
  (display prompt)
  (read-line))

(defn mult-str [amount string]
  (string/join (repeat amount string)))

(defn ord [chr]
  (int (.charAt chr 0)))

(defn chr [ord]
  (str (char ord)))

(defn random [seed]
  (proxy [java.util.Random][seed]
    (next [a] (proxy-super next a))))

(defn rand-float [random]
  (.nextFloat random))

(defn rand-game [game-data]
  (rand-float (game-data :rand)))

(defn coord-open? [coord empty-string]
  (cond
    (= (last coord) empty-string) true
    :else false))

(defn string->xy
  "Given a string coordinate, return the x and y components as a vector."
  [string-coord]
  (let [string-len (count string-coord)
        x-coord (take 1 string-coord)
        y-coord (take-last (dec string-len) string-coord)]
    [(string/join x-coord)
     (string/join y-coord)]))

(defn keyword->xy
  "Given a coordinate in keyword-form, return the x and y components as a
   vector."
  [keyword-coord]
  (-> keyword-coord
      (name)
      (string->xy)))

(defn xy->keyword
  "Given a sequence of two items, each representing an x and y value for a
  coordinate, convert to a keyword."
  [xy-pair]
  (-> xy-pair
      (string/join)
      (keyword)))

(defn move->string-coord
  "Give a player move (a string of the form 'yx'), convert it to a string
  coordinate (a string of the form 'xy')."
  [move]
  (let [x-coord (str (last move))
        [y-coord] (string/split move #"[a-z]")]
    (str x-coord y-coord)))

(defn move->keyword
  "Given a player move coordinate (a string of the form 'yx') convert to a
  keyword coordinate (of the form :xy)."
  [move]
  (-> move
      (move->string-coord)
      (string->xy)
      (xy->keyword)))

(defn get-friendly-coord
  "Given a coord (a keyword such as :a23), return a format that is easier for
  a player to read (by row, then columns)."
  [keyword-coord]
  (-> keyword-coord
      (keyword->xy)
      (reverse)
      (string/join)))

(defn is-item? [coord-data expeted-item-char]
  (= (last coord-data) expeted-item-char))

(defn filter-item
  "This function is intended to be used as a parameter passed to a map
  function."
  [coord-data expeted-item-char]
  (if (is-item? coord-data expeted-item-char)
    coord-data
    nil))

(def filter-star #(filter-item % (const/items :star)))
(def filter-empty #(filter-item % (const/items :empty)))
(def filter-outpost #(filter-item % (const/items :outpost)))

(defn filter-allowed [all legal]
  (intersection (set all) (set legal)))

(defn get-x-coord-range []
  (map
    chr
    (range
      const/xgrid-start
      const/xgrid-end)))

(defn get-y-coord-range []
  (range
    const/ygrid-start
    const/ygrid-end))

(defn in?
  "Given a sequence and a potential element of that sequence, determine if it
  is, in fact, part of that sequence."
  [sequence item]
  (if (empty? sequence)
    false
    (->> sequence
         (map #(= %1 item))
         (reduce #(or %1 %2)))))

(defn x-coord? [x-coord]
  (in? (get-x-coord-range) x-coord))

(defn y-coord? [y-coord]
  (in? (get-y-coord-range) y-coord))

(defn serialize-game-data [game-data]
  (conj game-data {:rand nil}))

(defn get-players [game-data]
  (game-data :players))

(defn get-player-count [game-data]
  (count (get-players game-data)))

(defn get-max-total-moves
  "This function should only be used when win-by-turns? is set to 'true'."
  ([game-data]
    (get-max-total-moves const/max-turns (get-player-count game-data)))
  ([max-turns player-count]
    (* max-turns player-count)))

(defn get-company-name
  "Given a letter, get the company name to which it corresponds."
  [letter-keyword]
  ((into
     {}
     (map
       (fn [x] [(keyword (str (first x))) x])
       const/companies))
   letter-keyword))

(defn get-companies
  "With no parameter, this function returns the names of all the potential
  companies for the game.

  If game-data is provided, it will instead return the names of just the
  companies that are on the board."
  ([]
    (take const/max-companies const/companies))
  ([game-data]
   (map
     (fn [x] (x :name))
     (game-data :companies))))

(defn get-companies-letters
  ([]
   (map (comp str first) (get-companies)))
  ([game-data]
   (map (comp str first) (get-companies game-data))))

(defn get-companies-keys []
  (map keyword (get-companies-letters)))

(defn count-occurances [data]
  (reduce
    #(assoc %1 %2 (inc (%1 %2 0)))
    {}
    data))

(defn get-map-of-maps
  ""
  ([map-keys]
    (get-map-of-maps map-keys {}))
  ([map-keys second-map]
    (into {} (map (fn [x] [x second-map]) map-keys))))

(defn company? [item]
  (in? (get-companies-letters) item))

(defn star? [item]
  (= item (const/items :star)))

(defn outpost? [item]
  (= item (const/items :outpost)))

(defn get-color-tuple
  ""
  [foreground-color background-color type]
  (str
    (const/color-info type)
    ";"
    (const/color-info :foreground)
    (const/color-info foreground-color)
    ;; :black or 0 is actually dark grey for backgrounds ... to get black,
    ;; don't specify a background color
    ;; ";"
    ;; (const/color-info :background)
    ;; (const/color-info background-color)
    ))

(defn start-color
  ""
  [foreground-color background-color type]
  (str
    const/open-color
    (get-color-tuple foreground-color background-color type)
    const/close-color))

(defn colorize
  ""
  ([text]
   (colorize text :white))
  ([text foreground & {:keys [background] :or {background :black}}]
   (let [[foreground-color type] (const/color-map foreground)
         [background-color _] (const/color-map background)]
     (str
       (start-color foreground-color background-color type)
       text
       const/end-color))))

(defn starts-with?
  "True if s starts with substr."
  {:added "1.8"}
  [^CharSequence s ^String substr]
  (.startsWith (.toString s) substr))
