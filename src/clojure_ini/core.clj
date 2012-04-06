(ns clojure-ini.core
  (:require [clojure.string :as s]
            [clojure.walk :as w]
            [clojure.java.io :as io]))

(defn- parse-key
  "Splits a key string by member-char and applies kw to each."
  [kw member-char k]
  (map kw (s/split k (re-pattern (str member-char)))))

(defn- parse-line [s kw trim member-char expand-members?]
  (if (= (first s) \[)
    (-> s (subs 1 (.indexOf s "]")) trim kw)
    (let [n (.indexOf s "=")]
      (if (neg? n)
        (throw (Exception. (str "Could not parse: " s)))
        (let [raw-key (-> s (subs 0 n) trim)]
          (conj 
            (if expand-members?
              (vec (parse-key kw member-char raw-key))
              [raw-key])
            (-> s (subs (inc n)) trim)))))))

(defn- strip-comment [s chr allow-anywhere?]
  (let [n (.indexOf s (int chr))]
    (if (and (not (neg? n))
             (or allow-anywhere?
                 (zero? n)))
      (subs s 0 n)
      s)))

(defn- mapify [coll]
  (loop [xs coll m {} key nil]
    (if-let [x (first xs)]
      (if (vector? x)
        (if (nil? key)
          (recur (rest xs)
                 (assoc-in m (butlast x) (last x))
                 key)
          (recur (rest xs)
                 (assoc-in m (into [key] (butlast x)) (last x))
                 key))
        (recur (rest xs)
               (assoc m x {})
               x))
      m)))

(defn- k->c [v]
  (read-string
    (if (keyword? v)
      (subs (str v) 1)
      (str v))))

(defn- listify
  "Walks m and converts all maps with only numerically parsable keys into sequences."
  [kw m]
  (w/postwalk
    #(if (map? %)
      (if (every? (comp integer? k->c) (keys (dissoc % (kw "size"))))
        (vals (into (sorted-map-by (fn [a b] (compare (k->c a) (k->c b)))) (dissoc % (kw "size")))) ;list
        (into (sorted-map) %)) ;normal map
      %) ;everything else
    m))

(defn read-ini
  "Read an .ini-file into a Clojure map.

  Valid options are:

  - keywordize? (default false): Turn segments and property-keys into
    keywords
  - trim? (default true): trim segments, keys and values
  - allow-comments-anywhere? (default true): Comments can appear
    anywhere, and not only at the beginning of a line
  - comment-char (default \\;)
  - expand-members? (default false): expands keys seperated by member-char into a map hierarchy
  - member-char (default \\/)
  - listify? (default false): converts maps in the result with only numericaly parsable keys into sequences"
  [in & {:keys [keywordize?
                trim?
                allow-comments-anywhere?
                comment-char
                expand-members?
                member-char
                listify?]
         :or {keywordize? false
              trim? true
              allow-comments-anywhere? true
              comment-char \;
              expand-members? false
              member-char \/
              listify? false}}]
  (let [kw (if keywordize? keyword identity)
        trim (if trim? s/trim identity)
        listify (if listify? listify #(identity %2))]
    (with-open [r (io/reader in)]
      (->> (line-seq r)
           (map #(strip-comment % comment-char allow-comments-anywhere?))
           (remove (fn [s] (every? #(Character/isWhitespace %) s)))
           (map #(parse-line % kw trim member-char expand-members?))
           mapify
           (listify kw)))))