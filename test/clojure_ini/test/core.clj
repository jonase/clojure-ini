(ns clojure-ini.test.core
  (:use [clojure-ini.core])
  (:use [clojure.test])
  (:use [clojure.pprint :only [pprint]])
  (:import java.io.StringWriter))

(defn pprint-str [s] 
  (let [w (StringWriter.)]
    (pprint s w)
    (.toString w)))

; TODO: Split into more specific smaller tests
(def test-string
"[base]
foo = 1
bar = Baz

[production]
database/host = 1.2.3.4
database/user = root
database/password = abcdef
debug/enabled = false

[development]
database/host = localhost
debug/enabled = true

[testing]
database/host = 5.5.5.5

[addresses]
size = 12
1 = 109.197.74.253
2 = 223.242.215.25
3 = 22.47.99.45
4 = 21.250.15.30
5 = 151.39.189.167
6 = 35.187.190.135
7 = 219.165.181.96
8 = 128.35.227.236
9 = 231.46.234.199
10 = 33.195.224.55
11 = 241.68.99.8
12 = 63.114.37.214

[users]
size = 3
0/name = \"Urist\"
0/pass = \"FooBar\"
1/name = \"John Doe\"
1/pass = \"password\"
2/name = \"MacFiddle\"
2/pass = \"123abc\"")

(def test-expected-result-default
  '{"addresses"
    {"11" "241.68.99.8",
     "size" "12",
     "12" "63.114.37.214",
     "1" "109.197.74.253",
     "2" "223.242.215.25",
     "3" "22.47.99.45",
     "4" "21.250.15.30",
     "5" "151.39.189.167",
     "6" "35.187.190.135",
     "7" "219.165.181.96",
     "8" "128.35.227.236",
     "9" "231.46.234.199",
     "10" "33.195.224.55"},
    "users"
    {"size" "3",
     "2/name" "\"MacFiddle\"",
     "1/name" "\"John Doe\"",
     "0/name" "\"Urist\"",
     "2/pass" "\"123abc\"",
     "1/pass" "\"password\"",
     "0/pass" "\"FooBar\""},
    "testing" {"database/host" "5.5.5.5"},
    "base" {"foo" "1", "bar" "Baz"},
    "production"
    {"debug/enabled" "false",
     "database/password" "abcdef",
     "database/host" "1.2.3.4",
     "database/user" "root"},
    "development" {"debug/enabled" "true", "database/host" "localhost"}})


(def test-expected-result-keywords-expanded
  '{:addresses
    ("109.197.74.253"
     "223.242.215.25"
     "22.47.99.45"
     "21.250.15.30"
     "151.39.189.167"
     "35.187.190.135"
     "219.165.181.96"
     "128.35.227.236"
     "231.46.234.199"
     "33.195.224.55"
     "241.68.99.8"
     "63.114.37.214"),
    :base {:bar "Baz", :foo "1"},
    :development
    {:database {:host "localhost"}, :debug {:enabled "true"}},
    :production
    {:database {:host "1.2.3.4", :password "abcdef", :user "root"},
     :debug {:enabled "false"}},
    :testing {:database {:host "5.5.5.5"}},
    :users
    ({:name "\"Urist\"", :pass "\"FooBar\""}
     {:name "\"John Doe\"", :pass "\"password\""}
     {:name "\"MacFiddle\"", :pass "\"123abc\""})})


(def test-expected-result-strings-expanded
  '{"addresses"
    ("109.197.74.253"
     "223.242.215.25"
     "22.47.99.45"
     "21.250.15.30"
     "151.39.189.167"
     "35.187.190.135"
     "219.165.181.96"
     "128.35.227.236"
     "231.46.234.199"
     "33.195.224.55"
     "241.68.99.8"
     "63.114.37.214"),
    "base" {"bar" "Baz", "foo" "1"},
    "development"
    {"database" {"host" "localhost"}, "debug" {"enabled" "true"}},
    "production"
    {"database" {"host" "1.2.3.4", "password" "abcdef", "user" "root"},
     "debug" {"enabled" "false"}},
    "testing" {"database" {"host" "5.5.5.5"}},
    "users"
    ({"name" "\"Urist\"", "pass" "\"FooBar\""}
     {"name" "\"John Doe\"", "pass" "\"password\""}
     {"name" "\"MacFiddle\"", "pass" "\"123abc\""})})


(deftest test-data-default
  (let [out (read-ini (.getBytes test-string))]
  (is (=  out test-expected-result-default)
      (pprint-str out))))

(deftest test-data-keywords-expanded
  (let [out (read-ini (.getBytes test-string)
                      :keywordize? true
                      :listify? true
                      :expand-members? true)]
    (is (= out test-expected-result-keywords-expanded)
      (pprint-str out))))

(deftest test-data-strings-expanded
  (let [out (read-ini (.getBytes test-string)
                      :keywordize? false
                      :listify? true
                      :expand-members? true)]
  (is (= out test-expected-result-strings-expanded)
      (pprint-str out))))