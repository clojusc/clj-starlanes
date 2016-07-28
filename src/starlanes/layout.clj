(ns starlanes.layout
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [starlanes.const :as const]
            [starlanes.util :as util]))


(def grid-space (util/mult-str const/grid-spaces \space))

(defn get-header []
  (string/join const/row-heading-init const/xgrid))

(defn get-row-header [row-grid-entry]
  (str row-grid-entry const/row-heading-init))

(defn get-row-header-buffer []
  (->> const/ygrid
       (take-last 1)
       (first)
       (get-row-header)
       (count)))

(defn print-grid-title [buffer-width grid-width]
  (let [mid-point (/ grid-width 2)
        title-len (count const/game-title)
        start-point (inc (- mid-point (/ title-len 2)))
        fill (util/mult-str (int start-point) \space)
        buffer (util/mult-str buffer-width \space)
        separator (util/mult-str title-len const/horiz-title-heading-char)]
    (util/display (str buffer fill const/game-title \newline))
    (util/display (str buffer fill separator \newline \newline))))

(defn print-xgrid-headers [buffer-length]
  (let [header (get-header)
        separator (util/mult-str (count header) const/horiz-divider-char)]
    (util/display (str (util/mult-str buffer-length \space)
                       header
                       \newline))
    (util/display (str (util/mult-str (- buffer-length 2) \space)
                       const/horiz-divider-init
                       separator
                       const/horiz-divider-term
                       \newline))))

(defn print-footer [buffer-length footer-length]
  (util/display (str (util/mult-str (- buffer-length 2) \space)
                     const/horiz-divider-init
                     (util/mult-str footer-length const/horiz-divider-char)
                     const/horiz-divider-term
                     \newline)))

(defn get-item-name [item]
  (if (util/company? item)
    :company
    ((set/map-invert const/items) item)))

(defn colorize-item [item]
  (->> item
       (get-item-name)
       (const/item-colors)
       (util/colorize item)))

(defn get-row-string [row-data]
  "'row-data' contains a list of keys (keywords) and values. To get the string
  content for the row, the values need to be extracted."
  (->> row-data
       (map (comp colorize-item second))
       (string/join grid-space)))

(defn get-row [row-key grouped-star-map]
  (-> grouped-star-map
      (get row-key)
      (sort)
      (get-row-string)))

(defn keyword-grouper
  "This function expects a list whose first element is a keyword (which will be
  used to group the associated data).

  The whole data structure that is being sorted is a (seq ...) of a map, a list
  of lists, where the sub-lists are pairs of keywords and single-character
  string values."
  [item]
  (-> item
      (first)
      (name)
      (first)
      (str)))

(defn grouper
  "This function expects a list whose first element is a keyword, and whose
  second element is a character (star-map 'item'). It is the second item by
  which the associated data will be grouped.

  'join' is used because there may be more than one digit after 'drop' is
  called -- which would result in a sequence being returned (and that would
  need to be 'join'ed).

  The whole data structure that is being sorted is a (seq ...) of a map, a list
  of lists, where the sub-lists are pairs of keywords and single-character
  string values."
  [item]
  (->> item
        (first)
        (name)
        (drop 1)
        (string/join)))

(defn print-rows [game-data]
  (let [star-map (group-by grouper (game-data :star-map))]
    (doseq [[row-num row-data] (into (sorted-map) star-map)]
      (util/display
        (str (get-row-header row-num)
             (get-row-string row-data)
             const/row-heading-term
             \newline)))))

(defn draw-grid [game-data]
  (util/clear-screen)
  (let [buffer-length (get-row-header-buffer)
        header-length (count (get-header))]
    (print-grid-title buffer-length header-length)
    (print-xgrid-headers buffer-length)
    (print-rows game-data)
    (print-footer buffer-length header-length)))
