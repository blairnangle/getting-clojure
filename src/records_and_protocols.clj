(ns records-and-protocols
  (:require [clojure.test :refer [deftest is testing]])
  (:import (clojure.lang ArityException PersistentArrayMap)))

(defrecord FictionalCharacter [name appears-in author])

(defrecord Employee [first-name last-name department])

(defprotocol Person
  (full-name [this])
  (greeting [this msg])
  (description [this]))

(defrecord FictionalCharacter1 [name appears-in author]
  Person
  (full-name [this] (:name this))
  (greeting [this msg] (str msg " " (:name this)))
  (description [this]
    (str (:name this) " is a character in " (:appears-in this))))

(defrecord Employee1 [first-name last-name department]
  Person
  (full-name [this] (str (:first-name this) " " (:last-name this)))
  (greeting [this msg] (str msg " " (:first-name this)))
  (description [this]
    (str (:first-name this) " " (:last-name this) " works in the " (:department this) " department")))

(defprotocol Marketable
  (make-slogan [this]))

;; this looks a bit like an inside-out defrecord
;; here, we are taking one protocol and defining its implementation across several record types
(extend-protocol Marketable
  String
  (make-slogan [s] (str s " is a String!"))
  FictionalCharacter1
  (make-slogan [fc] (str (:name fc) " is just *so* convincing in " (:appears-in fc) " by " (:author fc)))
  Employee1
  (make-slogan [e] (str "Employee " (:first-name e) " " (:last-name e) " is *so* good at their job!")))

(deftest tests
  (testing "records"
    ;; these two factory functions come for free once we create a record
    (let [watson (->FictionalCharacter "John Watson" "Sign of the Four" "Doyle")
          watson-1 (map->FictionalCharacter {:name       "John Watson"
                                             :appears-in "Sign of the Four"
                                             :author     "Doyle"})]
      (is (instance? FictionalCharacter watson))
      (is (= (class watson) FictionalCharacter))
      (is (not= (class watson) PersistentArrayMap))

      ;; remember that = in Clojure looks for structural equality - we care about the values, not the fact that they are two different instances
      (is (= watson watson-1))

      ;; regular map functions work on records
      (is (= (keys watson) [:name :appears-in :author]))

      ;; we can add to and remove from a record instance like a regular map
      (let [watson-plus (assoc watson :new-key "new val")
            watson-minus (dissoc watson :appears-in)]

        ;; an additive operation will not change the type of the object
        (is (instance? FictionalCharacter watson-plus))
        (is (= (class watson-plus) FictionalCharacter))

        ;; but a destructive operation will
        (is (instance? PersistentArrayMap watson-minus))
        (is (= (class watson-minus) PersistentArrayMap))))

    ;; attempting to construct a record with the wrong number or args will throw a clojure.lang.ArityException
    (is (thrown? ArityException (->FictionalCharacter "John Watson" "Sign of the Four" "Doyle" 1890)))

    ;; HOWEVER, using the map->… factory function will work
    ;; and the returned value will still be a FictionalCharacter record
    ;; this is equivalent to assoc-ing a new entry into a record after construction
    (is (class (map->FictionalCharacter {:name       "John Watson"
                                         :appears-in "Sign of the Four"
                                         :author     "Doyle"

                                         ;; extra key-value
                                         :published  1890}))
        FictionalCharacter)

    ;; if we pass in an incorrect key, we will still get a record back
    ;; but :author will be nil
    ;; this is not equivalent to dissoc-ing an entry from a record after construction
    (let [constructed-with-incorrect-key (map->FictionalCharacter {:name       "John Watson"
                                                                   :appears-in "Sign of the Four"
                                                                   ;:author     "Doyle"

                                                                   ;; incorrect key
                                                                   :written-by "Doyle"
                                                                   :published  1890})]
      (is (= (class constructed-with-incorrect-key) FictionalCharacter))
      (is (nil? (:author constructed-with-incorrect-key)))))

  (testing "protocols"
    (let [sam (->FictionalCharacter1 "Sam Weller" "The Pickwick Papers" "Dickens")
          sofia (->Employee1 "Sofia" "Diego" "Finance")]

      ;; let's observe polymorphism in action!
      (is (= (full-name sam) "Sam Weller"))
      (is (= (full-name sofia) "Sofia Diego"))
      (is (= (greeting sam "Well, hello there,") "Well, hello there, Sam Weller"))
      (is (= (greeting sofia "Howdy,") "Howdy, Sofia"))
      (is (= (description sam) "Sam Weller is a character in The Pickwick Papers"))
      (is (= (description sofia) "Sofia Diego works in the Finance department"))

      ;; let's try out our new protocol method implemented for several record types at once using extend-protocol
      (is (= (make-slogan "I am a String") "I am a String is a String!"))
      (is (= (make-slogan sam) "Sam Weller is just *so* convincing in The Pickwick Papers by Dickens"))
      (is (= (make-slogan sofia) "Employee Sofia Diego is *so* good at their job!")))))
