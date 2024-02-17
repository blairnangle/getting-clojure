(ns hello-clojure
  (:require [clojure.test :refer [deftest testing is]])
  (:import (clojure.lang Ratio)))

;; to invoke from the command line: clojure -M -m hello-clojure
(defn -main
  "I don't do a whole lot… yet."
  [& _args]
  (println "Hello, World!"))

(deftest tests
  (testing "division"
    (is (= (type (/ 8 3)) Ratio))
    (is (= (type (/ 8 2)) Long))
    (is (= (type (quot 8 3)) Long)))

  (testing "numeric type promotion"
    (is (= (type (+ 1 1)) Long))
    (is (= (type (+ 1 1.0)) Double))
    )
  )

;; def a placeholder symbol for a fn
(declare empty-fn-to-be-filled-in-later)

(defn use-empty-fn []
  (empty-fn-to-be-filled-in-later))

(defn empty-fn-to-be-filled-in-later []
  (println "filling in fn body *after* referencing it another fn"))

(comment

  (use-empty-fn)
  )
