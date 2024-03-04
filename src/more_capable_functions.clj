(ns more-capable-functions
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]))

(defn welcome-msg
  ([] (welcome-msg "stranger"))
  ([name] (str "Hello, " name "!")))

(defn takes-zero-or-more [& ints]
  (map inc ints))

(defn takes-one-or-more [int & ints]
  (map inc (cons int ints)))

(defn dispatch-book-format [book]
  (cond
    (vector? book) :vector-book
    (contains? book :title) :standard-map
    (contains? book :book) :alternative-map
    (contains? book :name-of-book) :keyword-for-which-no-method-implementation-exists))

;; we want all the books to end up in the same format but how we get there depends on the shape of the input
(defmulti normalize-book dispatch-book-format)

(defmethod normalize-book :vector-book [book]
  {:title (first book) :author (second book)})

(defmethod normalize-book :standard-map [book]
  book)

(defmethod normalize-book :alternative-map [book]
  {:title (:book book) :author (:by book)})

(defn dispatch-language [person]
  (let [country (:country person)]
    (cond
      (= country "France") :french
      (= country "Spain") :spanish
      (= country "Italy") :italian)))

(defmulti greet dispatch-language)

(defmethod greet :french [person]
  (str "Bonjour, " (:name person) "!"))

(defmethod greet :spanish [person]
  (str "Hola, " (:name person) "!"))

(defmethod greet :italian [person]
  (str "Buongiorno, " (:name person) "!"))

(defmethod greet :default [person]
  (str "Hello, " (:name person) "!"))

(defn reformat-and-publish-book
  "Expects a particular shape of input and output."
  [book]
  {:pre  [(and (contains? book :title) (contains? book :author))]
   :post [(and (contains? % :reformatted-title) (contains? % :reformatted-author))]}

  (println (str "original book: " book))

  (let [reformatted {:reformatted-title  (string/upper-case (:title book))
                     :reformatted-author (string/upper-case (:author book))}]

    (println (str "reformatted book: " reformatted))
    reformatted))

(deftest tests
  (testing "multi-arity function"
    (is (= (welcome-msg) "Hello, stranger!"))
    (is (= (welcome-msg "Alice") "Hello, Alice!")))

  (testing "variadic functions"
    (is (= (takes-zero-or-more) '()))
    (is (= (takes-zero-or-more 1) '(2)))
    (is (= (takes-zero-or-more 1 2) '(2 3)))
    (is (= (takes-zero-or-more 1 2 3) '(2 3 4)))

    (is (= (takes-one-or-more 1) '(2)))
    (is (= (takes-one-or-more 1 2) '(2 3)))
    (is (= (takes-one-or-more 1 2 3) '(2 3 4))))

  (testing "multimethod"
    (let [books [["1984" "Orwell"]
                 {:title "1984" :author "Orwell"}
                 {:book "1984" :by "Orwell"}]]
      (is (every? #(= % {:title "1984" :author "Orwell"}) (map normalize-book books)))

      ;; if the dispatch function returns nil, this results in an exception (if there is no :default implementation)
      (is (thrown? IllegalArgumentException (normalize-book {:random-key "random value" :another-random-key "another random value"})))

      ;; if there is no implementation for a dispatch return value, an exception is also thrown
      (is (thrown? IllegalArgumentException (normalize-book {:person "Blair" :name-of-book "The Year of Indian Cooking"}))))

    (is (= (greet {:name "Alice" :country "France"}) "Bonjour, Alice!"))
    (is (= (greet {:name "Alice" :country "Spain"}) "Hola, Alice!"))
    (is (= (greet {:name "Alice" :country "Italy"}) "Buongiorno, Alice!"))

    ;; we can use the :default keyword cover all other cases
    (is (= (greet {:name "Alice" :country "Anywhere Else in the World"}) "Hello, Alice!")))

  (testing "pre and post"
    (is (= (reformat-and-publish-book {:title "Death's End" :author "Cixin Liu"}) {:reformatted-title "DEATH'S END" :reformatted-author "CIXIN LIU"}))

    ;; throws a java.lang.AssertionError if the input or output don't look right
    ;(reformat-and-publish-book {:some-key "some value" :some-other-key "some other value"})
    ))
