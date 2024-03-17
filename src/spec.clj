(ns spec
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]))

(def number-greater-than-10 (spec/and number? #(> % 10)))

(def string-or-number (spec/or :a-string string? :a-number number?))

(def collection-of-strings (spec/coll-of string?))

(def s-n-s-n (spec/cat :s1 string? :n1 number? :s2 string? :n2 number?))

;; the keys we provide must be namespace-qualified
(def book-spec (spec/keys :req-un [::title
                                   ::author
                                   ::copies]))

;; this also works
(def book-spec-keys-from-another-ns (spec/keys :req-un [:tests/title
                                                        :tests/author
                                                        :tests/copies]))

;; register a spec with the global spec registry
(spec/def
  ::movie
  (spec/keys :req-un [::title
                      ::director
                      ::year]
             :opt-un [::starring]))

;; we can build composite specs
(spec/def ::title string?)
(spec/def ::author string?)
(spec/def ::artist string?)
(spec/def ::copies number?)
(spec/def ::starring string?)

(spec/def ::book (spec/keys :req-un [::title ::author ::copies]))
(spec/def ::song (spec/keys :req-un [::title ::artist ::copies]))

(spec/def ::inventory (spec/coll-of ::book))

(defn find-by-title [title inventory]
  {:pre [(spec/valid? ::title title)
         (spec/valid? ::inventory inventory)]}
  (some #(when (= (:title %) title) %) inventory))

(defn find-by-title-2 [title inventory]
  (some #(when (= (:title %) title) %) inventory))

;; use fdef to register the function with the global spec registry
(spec/fdef find-by-title-2
           :args (spec/cat :title ::title
                           :inventory ::inventory))

(defn book-blurb [book]
  (str "The best selling book " (:title book) " by " (:author book)))

;; this needs to be truthy for the function plus arguments to pass spec
(defn- check-return [{:keys [args ret]}]
  (let [author (-> args :book :author)]
    (not (neg? (.indexOf ret author)))))

(spec/fdef book-spec
           :args (spec/cat :book ::book)
           :ret (spec/and string? (partial re-find #"The best selling"))
           :fn check-return)

(deftest tests
  (testing "is"
    (is (spec/valid? int? 10))
    (is (not (spec/valid? int? 10.0)))
    (is (spec/valid? number? 10.0))
    (is (not (spec/valid? number? :something))))

  (testing "and"
    (is (not (spec/valid? number-greater-than-10 10.0)))
    (is (not (spec/valid? number-greater-than-10 "hello")))
    (is (spec/valid? number-greater-than-10 10.1)))

  (testing "or"
    (is (spec/valid? string-or-number 10))
    (is (spec/valid? string-or-number "hello"))

    ;; keywords are not strings
    (is (not (spec/valid? string-or-number :whatever)))

    ;; nether are characters
    (is (not (spec/valid? string-or-number \b))))

  (testing "collections"
    (is (spec/valid? collection-of-strings '("we" "are" "all" "strings")))
    (is (not (spec/valid? collection-of-strings '("we" "are" "all" :strings)))))

  (testing "cat"
    (is (spec/valid? s-n-s-n '("a string" 1 "another string" 2)))

    ;; mess up the order
    (is (not (spec/valid? s-n-s-n '(1 "a string" 2 "another string")))))

  (testing "keys, and :req-un and :opt-un"
    (is (spec/valid? book-spec {:title "2001" :author "Clarke" :copies 21}))
    (is (spec/valid? book-spec-keys-from-another-ns {:title "2001" :author "Clarke" :copies 21}))

    ;; an extra key is still valid
    (is (spec/valid? book-spec-keys-from-another-ns {:title "2001" :author "Clarke" :copies 21 :additional "whatever"}))

    ;; but a key removed is not
    (is (not (spec/valid? book-spec-keys-from-another-ns {:title "2001" :author "Clarke"})))

    ;; using a globally registered spec
    ;; note the namespace-qualified keyword to avoid clashes
    (is (spec/valid? ::movie {:title "Sicario" :director "Villeneuve" :year 2015}))
    (is (not (spec/valid? book-spec {:title "Sicario" :director "Villeneuve" :year 2015})))

    ;; using composite specs
    (is (spec/valid? ::song {:title "Dancing in the Moonlight" :artist "Thin Lizzy" :copies 450000}))
    (is (spec/valid? ::book {:title "2001" :author "Clarke" :copies 21}))

    ;; invalid value for an optional key
    (is (not (spec/valid? book-spec {:title "Sicario" :director "Villeneuve" :year 2015 :starring :blunt}))))

  (testing ":pre and :post, and fdef"
    (let [two-thousand-and-one {:title "2001" :author "Clarke" :copies 21}]

      ;; using :pre and :post to validate function input

      (is (thrown? AssertionError (find-by-title "a title" 1)))
      (is (thrown? AssertionError (find-by-title 1 [])))

      ;; this is ok
      (is (= (find-by-title "2001" [two-thousand-and-one])
             two-thousand-and-one))

      ;; this is also ok
      (is (= (find-by-title "2002" [two-thousand-and-one])
             nil))

      ;; using fdef to register the function with our global spec registry to validate input

      ;; we need to explicitly turn on checking for our function
      (spec-test/instrument 'spec/find-by-title-2)

      ;; these will (correctly) fail spec and error (not sure how to test…)
      ;(find-by-title-2 "a title" 1)
      ;(find-by-title 1 [])

      ;; this is ok
      (is (= (find-by-title-2 "2001" [two-thousand-and-one])
             two-thousand-and-one))

      ;; this is also ok
      (is (= (find-by-title-2 "2002" [two-thousand-and-one])
             nil))))

  (testing "property-based, generative testing with spec"

    ;; again, we need to explicitly instrument
    (spec-test/instrument 'spec/book-blurb)

    ;; this small bit of code does a lot of testing for us
    (is (spec-test/check))))

(comment
  (spec/explain number? 4)
  (spec/explain number-greater-than-10 4)
  (spec/explain number? "4")
  (spec/explain ::book {:title "2001" :author "Clarke"})
  (spec/explain ::book {:title "Dancing in the Moonlight" :artist "Thin Lizzy" :copies 450000})

  (spec/conform s-n-s-n '("Emma" 1815 "Jaws" 1974))
  (spec/conform s-n-s-n '("Emma" 1815 "Jaws" "1974"))
  )