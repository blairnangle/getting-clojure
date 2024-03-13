(ns tests
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :as ctest]))


(defn find-by-title [title books]
  (some #(when (= (:title %) title) %) books))

(defn number-of-copies-of
  [title books]
  (:copies (find-by-title title books)))

(def books
  [{:title "2001" :author "Clarke" :copies 21}
   {:title "Emma" :author "Austen" :copies 10}
   {:title "Misery" :author "King" :copies 101}])

(deftest test-finding-books
  (testing "Finding books"
    (is (not (nil? (find-by-title "Emma" books))))
    (is (nil? (find-by-title "gobbledygook" books))))

  (testing "Copies in inventory"
    (is (= 10 (number-of-copies-of "Emma" books)))))

(def title-gen (gen/such-that not-empty gen/string-alphanumeric))
(def author-gen (gen/such-that not-empty gen/string-alphanumeric))
(def copies-gen (gen/such-that (complement zero?) gen/nat))

(def book-gen
  (gen/hash-map :title title-gen
                :author author-gen
                :copies copies-gen))

(def inventory-gen
  (gen/not-empty (gen/vector book-gen)))

(def inventory-and-book-gen
  (gen/let [inventory inventory-gen
            book (gen/elements inventory)]
    {:inventory inventory
     :book      book}))

(ctest/defspec find-by-title-finds-books 50
  (prop/for-all [i-and-b inventory-and-book-gen]
    (= (find-by-title (-> i-and-b :book :title) (:inventory i-and-b))
       (:book i-and-b))))

;; gen/sample takes 10 by default
(comment
  (gen/sample gen/string-alphanumeric)

  (gen/sample title-gen)
  (gen/sample author-gen)
  (gen/sample copies-gen)

  (gen/sample book-gen)

  (gen/sample inventory-gen)

  (gen/sample inventory-and-book-gen)

  )
