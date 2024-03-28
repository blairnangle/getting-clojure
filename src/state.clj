(ns state
  (:require [clojure.test :refer [deftest testing is]]))

;; define an atom with an initial counter of zero
(def counter (atom 0))

(def by-title (atom {}))

(defn add-book [{:keys [title] :as book}]
  (swap! by-title #(assoc % title book)))

(defn del-book [title]
  (swap! by-title #(dissoc % title)))

(defn find-book [title]
  (get @by-title title))

(def total-copies (ref 0))

(def book-inventory (ref {}))

;; alter must happen within a dosync form
;; either all of our alters happen or none of them do
(defn add-book-ref [{:keys [title] :as book}]
  (dosync
    (alter book-inventory #(assoc % title book))
    (alter total-copies #(+ (:copies book) %))))

(def n-books (agent 0))

(def books-inventory-agent (agent {}))

(defn add-book-agent [{:keys [title] :as book}]
  (dosync
    (send
      books-inventory-agent
      (fn [inv]
        (println "some sort of side effect that will happen at most once per agent update")
        (Thread/sleep 1000)
        (assoc inv title book)))
    (send
      n-books
      (fn [n]
        (println "some sort of side effect that will happen at most once per agent update")
        (Thread/sleep 1000)
        (inc n)))))

(deftest tests
  (testing "atoms"

    ;; increment our atom by 1
    (swap! counter inc)
    (is (= @counter 1))

    ;; update it using something a bit fancier - note that we use the updated state from the inc above
    (swap! counter #(* (+ % 1) 7))
    (is (not= @counter 0))
    (is (= @counter 14))

    (add-book {:title "Sea of Tranquility"})
    (is (= @by-title {"Sea of Tranquility" {:title "Sea of Tranquility"}}))

    (del-book "Sea of Tranquility")
    (is (= @by-title {}))

    (add-book {:title "Getting Clojure"})
    (add-book {:title "Joel on Software"})

    (is (= (find-book "Joel on Software") {:title "Joel on Software"})))

  (testing "refs"
    (add-book-ref {:title "Life 3.0" :copies 1234})

    (is (= @total-copies 1234))
    (is (= @book-inventory {"Life 3.0" {:title  "Life 3.0"
                                        :copies 1234}})))

  (testing "agents"

    ;; our agents (probably) won't be updated by the time we deref and assert
    ;; but send updates the agents asynchronously, so we cannot guarantee either way
    (add-book-agent {:title "How to Lie with Maps"})
    (is (= @books-inventory-agent {}))
    (is (= @n-books 0))

    ;; sleeping for a couple of seconds should be enough for the previous update and new updates to be dequeued and applied to our agents
    (add-book-agent {:title "On Writing Well"})
    (Thread/sleep 3000)
    (is (= @books-inventory-agent {"On Writing Well"      {:title "On Writing Well"}
                                   "How to Lie with Maps" {:title "How to Lie with Maps"}}))
    (is (= @n-books 2))))
