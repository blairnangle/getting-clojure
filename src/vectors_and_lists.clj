(ns vectors-and-lists
  (:require [clojure.test :refer [deftest testing is]]))

(deftest tests
  (let [my-vector [0 1 2 3 4 5 6 7 8 9]
        my-list '(0 1 2 3 4 5 6 7 8 9)]
    (testing "construction"
      (is (= (vector 0 1 2 3 4 5 6 7 8 9) my-vector))
      (is (= (list 0 1 2 3 4 5 6 7 8 9) my-list)))

    (testing "index lookup"

      ;; vectors can be called as functions to perform index lookups
      (is (= (nth my-vector 3) (my-vector 3)))

      ;; lists cannot be called as functions to perform index lookups
      (is (thrown? Exception (my-list 3))))

    (testing "adding"

      ;; conj => add to end of vectors, beginning of lists
      (is (= (conj my-vector 10) [0 1 2 3 4 5 6 7 8 9 10]))
      (is (= (conj my-list 10) '(10 0 1 2 3 4 5 6 7 8 9)))

      ;; cons => always add to beginning
      (is (= (cons 10 my-vector) '[10 0 1 2 3 4 5 6 7 8 9]))
      (is (= (cons 10 my-list) '(10 0 1 2 3 4 5 6 7 8 9))))))
