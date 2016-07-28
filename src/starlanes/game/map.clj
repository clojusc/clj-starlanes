(ns starlanes.game.map
  (:require [clojure.math.combinatorics :as combi]
            [clojure.string :as string]
            [starlanes.const :as const]
            [starlanes.util :as util]))


(defn create-item [rand-float]
  (cond
    (<= rand-float const/star-rate) (const/items :star)
    :else (const/items :empty)))

(defn create-game-item [game-data]
  (create-item
    (util/rand-game game-data)))

(defn -create-star-map [game-data]
  (map
    #(reverse (conj % (create-game-item game-data)))
    (combi/cartesian-product const/ygrid const/xgrid)))

(defn -combine-coords
  ([data]
    (apply -combine-coords data))
  ([x y item]
    (let [y (const/make-y-coord y)]
      [(keyword (str x y)) item])))

(defn create-star-map [game-data]
  (->> game-data
       (-create-star-map)
       (map -combine-coords)
       (into (sorted-map))))

(defn update-coords
  "Return a new game-data data structure with a new item at the given
  coordinates."
  [keyword-coord coord-item game-data]
  (let [old-star-map (game-data :star-map)
        new-star-map (conj old-star-map [keyword-coord coord-item])]
    (conj game-data {:star-map new-star-map})))

(defn multi-update-coords
  ""
  [keyword-coords coord-item game-data]
  (let [keyword-coord (first keyword-coords)
        remaining (rest keyword-coords)]
    (cond
      (not (nil? keyword-coord))
        (multi-update-coords
          remaining
          coord-item
          (update-coords keyword-coord coord-item game-data))
      :else game-data)))

(defn get-empty-coord [coord-data]
  (if (util/coord-open? coord-data (const/items :empty))
    (first coord-data)))

(defn get-open-coords [game-data]
  (->> game-data
       :star-map
       (map get-empty-coord)
       (remove nil?)))

(defn get-item [keyword-coord game-data]
  ((game-data :star-map) keyword-coord))

(defn get-item-coords [item-char game-data]
  (->> game-data
       :star-map
       (map #(first (util/filter-item % item-char)))
       (remove nil?)))

(defn company-data? [coord-data]
  (util/in? (util/get-companies-letters) (second coord-data)))

(defn get-companies-data [game-data]
  (->> game-data
       :star-map
       (filter company-data?)
       (sort)))

(defn get-company-coords [game-data]
  (->> (util/get-companies-letters)
       (map #(get-item-coords % game-data))
       (flatten)
       (sort)))

(defn get-star-coords [game-data]
  (get-item-coords (const/items :star) game-data))

(defn get-outpost-coords [game-data]
  (get-item-coords (const/items :outpost) game-data))

(defn get-possible-neighbors [coord]
    [(dec coord) coord (inc coord)])

(defn get-possible-x-neighbors
  "This function takes a letter representing the x component of a coordinate
  pair and returns the legal neighbors."
  [x-coord]
  (->> x-coord
       (util/ord)
       (get-possible-neighbors)
       (map util/chr)
       (filter util/x-coord?)))

(defn get-possible-y-neighbors
  "This function takes a number representing the y component of a coordinate
  pair and returns the legal neighbors."
  [y-coord]
  (->> y-coord
       (Integer.)
       (get-possible-neighbors)
       (filter util/y-coord?)))

(defn get-neighbors-pairs
  "This function takes a keyword that represents a coordinate (e.g., :b23) and
  returns coordinates (pair-wise, e.g., [(x1 y1) (x2 y2) ...]) for the
  neighboring positions. The number of returned neighbors could range anywhere
  from 3 (when the given coorindate is in a corner of the map) to 8 (when the
  given coordinate is in the center of the map).
  "
  [keyword-coord]
  (let [[x-coord y-coord] (util/keyword->xy keyword-coord)
        x-neighbors (get-possible-x-neighbors x-coord)
        y-neighbors (get-possible-y-neighbors y-coord)]
    (->> y-neighbors
         (combi/cartesian-product x-neighbors)
         (remove #{[x-coord (Integer. y-coord)]}))))

(defn get-neighbors
  "This function takes a keyword that represents a coordinate (e.g., :b23) and
  returns coordinates (as a list of keywords, e.g., [:x1y1 :x2y2 ...]) for the
  neighboring positions. The number of returned neighbors could range anywhere
  from 3 (when the given coorindate is in a corner of the map) to 8 (when the
  given coordinate is in the center of the map).
  "
  [keyword-coord]
  (->> keyword-coord
       (get-neighbors-pairs)
       (map (comp keyword string/join))))

(defn get-item-neighbors [keyword-coord game-data]
  (map (fn [x] [x (get-item x game-data)])
       (get-neighbors keyword-coord)))

(defn get-neighbor-companies [keyword-coord game-data]
  (->> game-data
       (get-item-neighbors keyword-coord)
       (filter (comp util/company? second))))

(defn get-neighbor-stars [keyword-coord game-data]
  (->> game-data
       (get-item-neighbors keyword-coord)
       (filter (comp util/star? second))))

(defn get-neighbor-outposts [keyword-coord game-data]
  (->> game-data
       (get-item-neighbors keyword-coord)
       (filter (comp util/outpost? second))))

(defn near-item? [keyword-coord coords-for-items]
  (-> (map get-neighbors coords-for-items)
      (flatten)
      (util/in? keyword-coord)))

(defn next-to-company? [keyword-coord game-data]
  (near-item? keyword-coord (get-company-coords game-data)))

(defn next-to-star? [keyword-coord game-data]
  (near-item? keyword-coord (get-star-coords game-data)))

(defn next-to-outpost? [keyword-coord game-data]
  (near-item? keyword-coord (get-outpost-coords game-data)))




