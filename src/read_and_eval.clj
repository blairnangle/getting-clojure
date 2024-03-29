(ns read-and-eval
  (:require [clojure.test :refer [deftest is testing]])
  (:import (java.io FileReader PushbackReader)))

(comment

  ;; these two forms have the same effect
  (read (PushbackReader. (FileReader. "src/authors.txt")))
  (read-string "words")

  ;; if we evaluate this data structure to a function in a comment/REPL
  (eval '(defn hey []
           (println "hey")))

  ;; then we can immediately call it
  (hey)

  ;; this is also a valid, if rather bizarre and silly, way to define and call a fn
  (def fn-name 'print-greeting)
  (def args (vector 'preferred-customer))
  (def the-println (list 'println "Welcome back!"))
  (def body (list 'if 'preferred-customer the-println))

  (eval (list 'defn fn-name args body))
  (eval (list 'print-greeting true))
  )

;; we can decorate a var or function with metadata
(def ^:greetings welcome "welcome to the party")
(defn ^:whatever howdy "howdy says howdy." [] (println "howdy!!!"))

(deftest tests
  (testing "read"

    ;; let's convert some strings into Clojure data structures
    (is (= (read-string "#{1 2 3 4}") (hash-set 1 2 3 4)))
    (is (= (read-string "[1 2 3 4]") (vector 1 2 3 4)))
    (is (= (read-string "{:title \"Chaos Monkeys\" :price 18.99}") (hash-map :title "Chaos Monkeys" :price 18.99))))

  (testing "eval"
    (is (= (eval '(+ 1 1)) 2))
    (let [fn-as-str "(defn greet [is-preferred-customer]
                                  (if is-preferred-customer
                                    \"Welcome back!\"
                                    \"Hello?\"))"
          fn-as-data-structure '(defn greet [is-preferred-customer]
                                  (if is-preferred-customer
                                    "Welcome back!"
                                    "Hello?"))]

      ;; use read to convert a string to a list
      (is (= (read-string fn-as-str) fn-as-data-structure))

      ;; use eval to turn the data structure into a fn, and then immediately call it
      (is (= ((eval fn-as-data-structure) true) "Welcome back!")))

    ;; eval does not know about local let bindings - the below code will not compile
    #_(let [x 100]
        (is (= (eval '(+ x 1)) 101))))

  (testing "meta"
    (let [metadata-map (meta #'welcome)]

      ;; metadata that we have added - :greetings will be true
      (is (:greetings metadata-map))

      ;; default stuff for a var
      (is (contains? metadata-map :line))
      (is (contains? metadata-map :column))
      (is (contains? metadata-map :file))
      (is (contains? metadata-map :name))
      (is (contains? metadata-map :ns)))

    (let [metadata-map (meta #'howdy)]

      ;; metadata that we have added - :whatever will be true
      (is (:whatever metadata-map))
      (is (= (:doc metadata-map) "howdy says howdy."))

      ;; default stuff for a fn
      (is (contains? metadata-map :arglists))
      (is (contains? metadata-map :line))
      (is (contains? metadata-map :column))
      (is (contains? metadata-map :file))
      (is (contains? metadata-map :name))
      (is (contains? metadata-map :ns)))))
