(ns logic
  (:require [clojure.test :refer [deftest testing is]])
  (:import (clojure.lang ExceptionInfo)))

(defn if-single-branch [x]
  (if (< x 10)
    "less"))

(defn if-double-branch [x]
  (if (< x 10)
    "less"
    "more"))

(defn evaluate-predicate [pred]
  (if pred
    "predicate was truthy"
    "predicate was falsy"))

(defn use-cond [s]
  (cond
    (= s "a") "string is a"
    (= s "b") "string is b"
    :else "string is neither a nor b"))

;; note the absence of a predicate value/:else for the last expression
(defn use-case [s]
  (case s
    "a" "string is a"
    "b" "string is b"
    "string is neither a nor b"))

(defn try-catch-finally [n]
  (try (/ 100 n)
       (catch ArithmeticException ae
         (println (str "we threw an ArithmeticException:" ae))
         "return value after catching an ArithmeticException")
       (catch Exception e
         (println (str "we threw an exception that wasn't an ArithmeticException:" e))
         "return value after catching any other Exception")
       (finally
         (println "do a side effect regardless of what was thrown or caught"))))

(defn manually-throw [s]
  (if (= s "should throw")
    (throw
      (ex-info "throwing a Clojure exception with" {:s s}))
    "didn't throw anything"))

(deftest tests
  (testing "if-else"

    ;; without an else-branch nil is returned
    (is (nil? (if-single-branch 11)))

    ;; standard else-branch logic
    (is (= (if-double-branch 11) "more")))

  (testing "truthy and falsy"

    ;; almost everything in Clojure is truthy
    (is (= (evaluate-predicate "a string") "predicate was truthy"))
    (is (= (evaluate-predicate 42) "predicate was truthy"))
    (is (= (evaluate-predicate ["i" "am" "a" "truthy" "vec"]) "predicate was truthy"))
    (is (= (evaluate-predicate :keywords-are-truthy) "predicate was truthy"))

    ;; even empty collections
    (is (= (evaluate-predicate []) "predicate was truthy"))
    (is (= (evaluate-predicate '()) "predicate was truthy"))
    (is (= (evaluate-predicate {}) "predicate was truthy"))
    (is (= (evaluate-predicate #{}) "predicate was truthy"))

    ;; false and nil are falsy
    (is (= (evaluate-predicate false) "predicate was falsy"))
    (is (= (evaluate-predicate nil) "predicate was falsy")))

  (testing "do and when"

    ;; do is useful for wrapping a bunch of (side-effecting) things together
    (is (= (do (println "first side effect")
               (println "second side effect")
               "return me")
           "return me"))

    ;; when is like a single-branch if that allows multiple expressions to be wrapped together (without the need for an explicit do)
    (is (= (when "strings are truthy"
             (println "first side effect")
             (println "second side effect")
             "return me")
           "return me"))

    ;; like if without an else-branch, when returns nil if the predicate is falsy
    (is (= (when nil
             (println "first side effect")
             (println "second side effect")
             "return me")
           nil)))

  (testing "cond and case"

    ;; we can think of case as a more opinionated version of cond
    (is (= (use-case "a") (use-cond "a")))
    (is (= (use-case "b") (use-cond "b")))
    (is (= (use-case "c") (use-cond "c"))))

  ;; note that the finally form is just for side effects, not returning a value
  (testing "try-catch-finally"

    ;; return value is the result of division
    (is (= (try-catch-finally 5) 20))

    ;; return value can come from the catch form
    (is (= (try-catch-finally 0) "return value after catching an ArithmeticException"))
    (is (= (try-catch-finally "string") "return value after catching any other Exception"))

    ;; manually throw a clojure.lang.ExceptionInfo
    (is (thrown? ExceptionInfo (manually-throw "should throw")))
    (is (manually-throw "shouldn't throw") "didn't throw anything")))
