(ns threads-promises-and-futures
  (:require [clojure.test :refer [deftest testing is]]
            [clj-time.core :as clj-time]))

(defn sleep-double-and-print [n]
  (let [double (* n 2)
        sleep-time (* double 1000)]
    (Thread/sleep ^long sleep-time)
    (println "I am a thread that has just slept for" sleep-time "milliseconds; double n is:" double)
    double))

(defn sleep-triple-and-print [n]
  (let [triple (* n 3)
        sleep-time (* triple 1000)]
    (Thread/sleep ^long sleep-time)
    (println "I am a thread that has just slept for" sleep-time "milliseconds; triple n is:" triple)
    triple))

(comment
  ;; .start will immediately return nil to the REPL (void method)
  ;; while printing (in a separate thread) will take a few seconds
  ;; we won't see printing at all in tests, just in the REPL
  (.start (Thread. #(sleep-double-and-print 1)))

  ;; we can reach out to multiple threads to do our grunt work
  (do
    (.start (Thread. #(sleep-double-and-print 1)))
    (.start (Thread. #(sleep-triple-and-print 1))))
  )

(def fav-book "Jaws")

;; Naughty! Purely for illustrative purposes!
(defn make-emma-favourite []
  (def fav-book "Emma"))

;; Naughty! Purely for illustrative purposes!
(defn make-2001-favourite []
  (def fav-book "2001"))

(def ^:dynamic *my-favourite-book* "The Bridge")

(def thread-1
  (Thread.
    #(binding [*my-favourite-book* "Infinite Jest"]
       (println "My favourite book is" *my-favourite-book*))))

(def thread-2
  (Thread.
    #(binding [*my-favourite-book* "Rubicon"]
       (println "My favourite book is" *my-favourite-book*))))

(comment
  ;; we would expect 2001 to be printed, but that is not guaranteed!
  ;; here, we have created a race condition - albeit a trivial one
  (.start (Thread. make-emma-favourite))
  (.start (Thread. make-2001-favourite))
  (println "my favourite book is" fav-book)


  (.start thread-1)

  ;; the binding within this thread will not be influenced by the thread above
  (.start thread-2)
  )

(def inventory [{:name "The Bridge" :price 50 :copies 100}
                {:name "Infinite Jest" :price 25 :copies 200}
                {:name "Rubicon" :price 5 :copies 400}])

(defn sum-price [inv]
  (reduce + (map :price inv)))

(defn sum-copies [inv]
  ;; sleep for 5 seconds to prove that our main thread will wait for a result after deref
  (Thread/sleep 5000)
  (reduce + (map :copies inv)))

(def my-future (future
                 (Thread/sleep 3000)
                 (+ 2 2)))

(defn slow-add-one [n]
  (Thread/sleep 2000)
  (+ n 1))

(deftest tests
  (testing "promises"
    (let [my-promise (promise)]
      (deliver my-promise "the value getting delivered to my promise")

      (is (= (deref my-promise) "the value getting delivered to my promise")))

    ;; create promises to allow for multi-threaded computation of results
    (let [total-price (promise)
          total-copies (promise)]

      ;; spin up separate threads to the do the computation and deliver values to our promises
      (.start (Thread. #(deliver total-price (sum-price inventory))))
      (.start (Thread. #(deliver total-copies (sum-copies inventory))))

      ;; upon deref-ing our promises, the main thread will wait in turn until each has a value
      (is (= (deref total-price) 80))
      (is (= (deref total-copies) 700))))

  (testing "futures"
    ;; @ on a promise or future (or atom, ref or agent) is shorthand for deref
    ;; no need to explicitly deliver a result - Clojure takes care of that for us
    ;; this will take a while, but we'll get the result eventually
    (is (= @my-future 4))))

;; pmap is going to use a thread pool behind the scenes to compute the result (and return more quickly)
(comment
  (println (clj-time/now))
  (map slow-add-one [0 1 2 3 4 5 6 7 8 9])
  (println (clj-time/now))
  (pmap slow-add-one [0 1 2 3 4 5 6 7 8 9])
  (println (clj-time/now))
  )
