(ns functional-things
  (:require [clojure.test :refer [deftest is testing]]))

(defn cheap? [book]
  (<= (:price book) 9.99))

(defn pricey? [book]
  (> (:price book) 9.99))

(defn horror? [book]
  (= (:genre book) :horror))

(defn adventure? [book]
  (= (:genre book) :adventure))

(defn all? [book & predicates]
  (every? true? (map #(% book) predicates)))

(defn affordable? [max-price]
  (fn [book]
    (<= (:price book) max-price)))

(def affordable-for-students? (affordable? 3))

(def affordable-for-professionals? (affordable? 25))

(defn cheaper-than? [max-price book]
  (<= (:price book) max-price))

(def real-cheap? (partial cheaper-than? 1.00))

(def kind-of-cheap? (partial cheaper-than? 1.99))

(def marginally-cheap? (partial cheaper-than? 5.99))

;; every-pred returns a function that evaluates - within an (and) form - all the predicates passed to it
(def cheap-horror? (every-pred cheap? horror?))

(deftest tests
  (let [dracula {:title  "Dracula"
                 :author "Stoker"
                 :price  1.99
                 :genre  :horror}
        sea-of-tranquility {:title  "Sea of Tranquility"
                            :author "St John Mandel"
                            :price  15.0
                            :genre  :sci-fi}]
    (testing "functions as arguments"
      (is (true? (all? dracula cheap?)))
      (is (true? (all? dracula cheap? horror?)))
      (is (false? (all? dracula cheap? pricey?)))
      (is (false? (all? dracula adventure?))))

    (testing "composite functions"
      (is (true? (affordable-for-students? dracula)))
      (is (false? (affordable-for-students? sea-of-tranquility)))
      (is (true? (affordable-for-professionals? dracula)))
      (is (true? (affordable-for-professionals? sea-of-tranquility))))

    (testing "apply"
      (is (= (apply + [1 2 3]) (+ 1 2 3)))

      ;; a singly nested collection is fine for apply
      (is (= (apply + 1 2 3 [4 5 6]) (+ 1 2 3 4 5 6)))

      ;; but a doubly nested vector will not be unwrapped enough for [7] to be passed to +
      (is (thrown? ClassCastException (apply + 1 2 3 [4 5 6 [7]])))

      ;; applying str to sequence is the same as passing all the elements of the seq to str
      (is (= (apply str ["Hello " "there, " "Blair!"]) (str "Hello " "there, " "Blair!")))

      ;; but this will not hold for maps - remember that Clojure sometimes treat maps as a sequence of key-value pairs
      (is (not= (str dracula) (apply str dracula)))

      ;; apply is useful if we don't know what the arguments to a function will be at compile-time,
      ;; so we might be unable to write out each argument to a function,
      ;; but we can use apply to cover this case
      (let [my-list [122 "abc" 45 "73847" "random string"]]
        (is (= (list 122 "abc" 45 "73847" "random string") (apply list my-list)))))

    (testing "partial"
      (is (false? (real-cheap? dracula)))
      (is (false? (real-cheap? sea-of-tranquility)))
      (is (true? (kind-of-cheap? dracula)))
      (is (false? (kind-of-cheap? sea-of-tranquility)))
      (is (true? (marginally-cheap? dracula)))
      (is (false? (marginally-cheap? sea-of-tranquility))))

    (testing "complement"

      ;; complement is equivalent to wrapping the supplied function in (not)
      (let [not-real-cheap? (complement real-cheap?)
            not-kind-of-cheap? (complement kind-of-cheap?)
            not-marginally-cheap? (complement marginally-cheap?)]
        (is (true? (not-real-cheap? dracula)))
        (is (false? (not-kind-of-cheap? dracula)))
        (is (false? (not-marginally-cheap? dracula)))))

    (testing "every-pred"
      (is (cheap-horror? dracula))
      (is (not (cheap-horror? sea-of-tranquility))))

    (testing "anonymous functions"
      (let [my-vec [0 1 2 3 4 5 6 7 8 9]]

        ;; we can use named parameters or % placeholders
        (is (= (reduce (fn [a b] (+ a b)) my-vec)
               (reduce #(+ %1 %2) my-vec)))))))
