(ns let
  (:require [clojure.test :refer [deftest testing is]]))

(def users-discounts {"Nicholas" 0.10
                      "Jonathan" 0.07
                      "Felicia"  0.05})

;; we can use the combination of let and fn to take some values and compute some others
;; and then return a very narrowly scoped function, ready to go with these values set
(defn mk-discount-price-f [user-name user->discounts min-charge]
  (let [discount-% (get user->discounts user-name)]
    (fn [amount]
      (let [discount (* amount discount-%)
            discounted-amount (- amount discount)]
        (if (< discounted-amount min-charge)
          min-charge
          discounted-amount)))))

;; this is our narrowly scoped function, ready to compute the price Felicia needs to pay
(def compute-felicia-price (mk-discount-price-f "Felicia" users-discounts 7.00))

(deftest tests
  (testing "anonymous function created within let"
    (is (= (compute-felicia-price 6.00) 7.00))
    (is (= (compute-felicia-price 10.00) 9.50)))

  (testing "if-let, when-let"
    (let [default-number 42]

      ;; if the expression evaluates to truthy, if-let binds the symbol to the result of the expression
      (is (= (if-let [my-number (+ 1 2)]
               my-number
               default-number)
             3))

      ;; if the expression evaluates to falsy, if-let returns the else-branch
      (is (= (if-let [my-number nil]
               my-number
               default-number)
             42))

      ;; if the expression evaluates to truthy, when-let binds the symbol to the result of the expression
      (is (= (when-let [my-number (+ 1 2)]
               my-number)
             3))

      ;; if the expression evaluates to falsy, when-let returns nil
      (is (= (when-let [my-number nil]
               my-number)
             nil)))))
