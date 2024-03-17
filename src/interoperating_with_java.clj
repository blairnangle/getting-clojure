(ns interoperating-with-java
  (:require [clojure.test :refer [deftest is testing]])
  (:import (com.google.gson Gson)
           (java.awt Rectangle)
           (java.io File)))

(def authors (File. "src/authors.txt"))

(def rect (Rectangle. 0 0 10 20))

(def my-file (File/createTempFile "my-file" ".txt"))

(def gson-obj (Gson.))

(deftest tests
  (testing "accessing a Java object's fields"
    (is (= (.-width rect) 10))
    (is (= (.-height rect) 20)))

  (testing "using a static method"
    (is (.startsWith (.getName my-file) "my-file"))
    (is (.endsWith (.getName my-file) ".txt")))

  (testing "using a third-party Java lib"
    (is (= (.toJson gson-obj {:my-key 44}) "{\":my-key\":44}")))

  (testing "calling a Java method on a Clojure var"
    (let [my-vec [1 2 3]]
      (is (= (.count my-vec) 3)))))

(comment

  ;; using Clojure to get the file's contents
  ;; note that the relative path is from wherever project.clj lives, generally
  (slurp "src/authors.txt")

  (if (.exists authors)
    (println "Our authors file is there.")
    (println "Our authors file is missing."))

  (if (.canRead authors)
    (println "We can read it!")
    (println "We cannot"))

  (.setReadable authors true)
  )
