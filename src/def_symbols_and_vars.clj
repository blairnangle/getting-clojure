(ns def-symbols-and-vars
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]))

(def describe-book-1
  (fn [book]
    (str (:title book)
         " by "
         (:author book))))

(defn describe-book-2 [book]
  (str (:title book)
       " by "
       (:author book)))

(def ^:dynamic *describe-book-1-dynamic*
  (fn [book]
    (str (:title book)
         " by "
         (:author book))))

(def ^:dynamic *the-number-three* 3)

(let [the-bridge {:title  "The Bridge"
                  :author "Iain Banks"}]
  (deftest tests
    (testing "def + fn = defn"
      (is (= (describe-book-1 the-bridge)
             (describe-book-2 the-bridge))))

    (testing "symbols can be values"
      (is (= (take 2 (string/split (str 'describe-book-1) #"-"))
             (take 2 (string/split (str 'describe-book-2) #"-")))))

    (testing "vars represent symbol-value bindings; their values can be accessed"

      ;; vars come fully qualified with the namespace
      (is (= (str #'describe-book-1) "#'def-symbols-and-vars/describe-book-1")))

    (testing "using binding to update a dynamic var's value within a lexical scope"

      (is (= (*describe-book-1-dynamic* the-bridge)
             "The Bridge by Iain Banks"))

      ;; works like let but with binding we are refining an existing var
      (binding [*describe-book-1-dynamic* (fn [book] (str "A book called " (:title book) " by the author " (:author book)))]

        (is (= (*describe-book-1-dynamic* the-bridge)
               "A book called The Bridge by the author Iain Banks"))

        (is (not= (*describe-book-1-dynamic* the-bridge)
                  "The Bridge by Iain Banks"))))

    (testing "set!"

      ;; our dynamic var should still have its original value
      (is (= *the-number-three* 3))

      ;; updated within a binding
      (binding [*the-number-three* 4]
        (is (= *the-number-three* 4))

        ;; updated again by set!
        (set! *the-number-three* 5)
        (is (= *the-number-three* 5))))))
