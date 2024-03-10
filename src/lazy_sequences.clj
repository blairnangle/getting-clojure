(ns lazy-sequences
  (:require [clojure.test :refer [deftest testing is]]))

(def jack "All work and no play makes Jack a dull boy.")

(def repeated-jack (repeat jack))

(defn chatty-vec []
  (println "Here we go!")
  [1 2 3])

(deftest tests
  (testing "repeat, cycle and iterate"
    (is (= (first repeated-jack) jack))
    (is (= (second repeated-jack) jack))
    (is (= (nth repeated-jack 1) jack))

    ;; this is surprisingly fast!
    (is (= (nth repeated-jack 1000000) jack))

    ;; this would never finish
    #_(is (= (last repeated-jack) jack))

    ;; use cycle to create cyclic sequences
    (is (= (take 3 (cycle [1 2 3])) [1 2 3]))
    (is (= (take 6 (cycle [1 2 3])) [1 2 3 1 2 3]))

    ;; use iterate to build an unbounded seq with a function
    (is (= (nth (iterate inc 1) 100) 101))

    ;; observe the power of exponents
    (is (= (nth (iterate #(* 2 %) 2) 4) 32))
    (is (= (nth (iterate #(* 2 %) 2) 10) 2048)))

  (testing "when does a lazy seq actually get computed?"

    ;; we will not see "Here we go!" printed when we bind a lazy seq to a symbol
    (let [lazy-though (lazy-seq (chatty-vec))]

      ;; we *will* see "Here we go!" printed when we actually try to pull some data out of the lazy seq
      (is (= (first lazy-though) 1))))

  (testing "repeatedly"

    ;; the fn we pass to repeatedly should take no-args (e.g., GET from an API) and adds each result to a lazy seq,
    ;; but we don't actually make any API calls until we actually try to get some data out of the lazy seq
    (let [first-chars-from-blairnangle-dot-com (repeatedly #(subs (slurp "https://blairnangle.com") 0 15))]

      ;; this will result in 10 API calls to blairnangle.com
      (is (= (nth first-chars-from-blairnangle-dot-com 10) "<!DOCTYPE html>")))))
