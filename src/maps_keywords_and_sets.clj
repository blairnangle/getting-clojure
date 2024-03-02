(ns maps-keywords-and-sets
  (:require [clojure.test :refer [deftest testing is]]))

(deftest tests
  (let [map-literal {"title"     "Oliver Twist"
                     "author"    "Dickens"
                     "published" 1838}
        hash-map-constructed-map (hash-map "title" "Oliver Twist"
                                           "author" "Dickens"
                                           "published" 1838)
        keyword-map {:title     "Oliver Twist"
                     :author    "Dickens"
                     :published 1838}
        a-vec ["Oliver Twist" "Dickens" 1838]
        a-set #{"Dickens" "Austen"}]
    (testing "construction"
      (is (= map-literal hash-map-constructed-map)))

    (testing "lookup"
      (is (= (get map-literal "title") (map-literal "title")))
      (is (= (get map-literal "key that doesn't exist") nil))
      (is (= (get map-literal "title") (:title keyword-map))))

    (testing "assoc and dissoc"

      ;; applied to maps
      (is (= (assoc keyword-map :rating 8 :language "English") {:title     "Oliver Twist"
                                                                :author    "Dickens"
                                                                :published 1838
                                                                :rating    8
                                                                :language  "English"}))
      (is (= (dissoc keyword-map :title :author :published) {}))

      ;; assoc applied to vectors, but only when "inserting" (replacing) or appending directly to the end
      (is (= (assoc a-vec 0 "something") ["something" "Dickens" 1838]))
      (is (= (assoc a-vec 0 "something" 1 "Tolstoy") ["something" "Tolstoy" 1838]))
      (is (= (assoc a-vec 3 "something else") ["Oliver Twist" "Dickens" 1838 "something else"]))
      (is (thrown? Exception (assoc a-vec 4 "something else")))

      ;; dissoc cannot be used on vectors
      (is (thrown? Exception (dissoc a-vec 0))))

    (testing "sorted"
      (let [map-sorted (sorted-map :title "Oliver Twist"
                                   :author "Dickens"
                                   :published 1838)]

        ;; uses the implicit/natural ordering of the keys for sorting
        (is (= (keys map-sorted) [:author :published :title]))
        (is (= (vals map-sorted) ["Dickens" 1838 "Oliver Twist"]))))

    (testing "sets"

      ;; attempting to create a set literal results in a compilation error
      ;#{"Dickens" "Austen" "Dickens"}

      ;; adding a duplicate element to a set just returns the original set
      (is (= #{"Dickens" "Austen"} (conj a-set "Dickens")))

      ;; ordering is not important
      (is (= #{"Dickens" "Austen"} #{"Austen" "Dickens"}))

      ;; us disj for element removal
      (is (= (disj a-set "Austen") #{"Dickens"}))

      ;; contains? for testing set membership
      (is (contains? a-set "Austen"))

      ;; we can also call a set as a function to return an element (and determine membership
      (is (= "Austen" (a-set "Austen")))
      (is (= nil (a-set "Someone Else"))))))
