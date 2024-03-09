(ns sequences
  (:require
    [clojure.test :refer [deftest testing is]]))

(defn cheap?
  "Return the book if it is cheap.
  Return nil if the book is not cheap."
  [book]
  (when (<= (:price book) 9.99)
    book))

(defn extremely-cheap?
  "Return the book if it is extremely cheap.
  Return nil if the book is not extremely cheap."
  [book]
  (when (<= (:price book) 0.99)
    book))

(defn hi-price
  "If the book's price is higher than the current high price, return the book's price.
  If not, return the current high price."
  [current-hi book]
  (let [book-price (:price book)]
    (if (> book-price current-hi)
      book-price
      current-hi)))

(defn three-highest-rated [books]
  (apply str
         (interpose " // "
                    (take 3 (map :title (reverse (sort-by :rating books)))))))

(defn three-highest-rated->> [books]
  (->> books
       (sort-by :rating)
       (reverse)
       (map :title)
       (take 3)
       (interpose " // ")
       (apply str)))

(defn three-highest-rated-> [books]
  (->> books
       (sort-by :rating)
       (reverse)
       (map :title)
       (take 3)
       (interpose " // ")
       (apply str)))

(deftest tests
  (let [unordered-0->9 [5 9 8 1 0 3 2 4 7 6]
        unordered-a->j ["b" "f" "d" "j" "a" "c" "e" "h" "g" "i"]
        books [{:title "Deep Six" :price 13.99 :genre :sci-fi :rating 6}
               {:title "Dracula" :price 1.99 :genre :horror :rating 7}
               {:title "Emma" :price 7.99 :genre :comedy :rating 9}
               {:title "2001" :price 10.50 :genre :sci-fi :rating 5}]]
    (testing "emptiness; rest and next"

      ;; seq returns nil on any empty collection
      (is (= (seq {}) nil))
      (is (= (seq []) nil))
      (is (= (seq '()) nil))
      (is (= (seq #{}) nil))

      ;; rest and next do the same thing…
      (is (= (rest '(1 2 3 4)) '(2 3 4)))
      (is (= (next '(1 2 3 4)) '(2 3 4)))

      ;; …except on empty collections
      (is (= (rest '()) '()))
      (is (= (next '()) nil)))

    (testing "handy seq functions"

      ;; sort does what we expect using what it deems to be the natural order of the coll's elements
      (is (= (sort unordered-0->9) '(0 1 2 3 4 5 6 7 8 9)))

      ;; sort-by allows us to provide a map-lookup function - in this case we just use keyword-as-function
      (is (= (map :price (sort-by :price books)) '(1.99 7.99 10.50 13.99)))

      ;; can be handily paired with reverse
      (is (= (reverse unordered-0->9) '(6 7 4 2 3 0 1 8 9 5)))
      (is (= (reverse (sort unordered-0->9)) '(9 8 7 6 5 4 3 2 1 0)))

      ;; partition for… chunking/partitioning
      ;; note that the nested lists do not need the leading '
      (is (= (partition 2 (sort unordered-0->9)) '((0 1) (2 3) (4 5) (6 7) (8 9))))

      ;; interleave to combine every other element with that of another collection, returning a single coll
      (is (= (interleave
               (sort unordered-0->9)
               (sort unordered-a->j))
             '(0 "a" 1 "b" 2 "c" 3 "d" 4 "e" 5 "f" 6 "g" 7 "h" 8 "i" 9 "j")))

      ;; interpose to do similarly to interleave but insert a constant (separator) between each element, returning a coll
      (is (= (interpose "-" (sort unordered-a->j)) '("a" "-" "b" "-" "c" "-" "d" "-" "e" "-" "f" "-" "g" "-" "h" "-" "i" "-" "j")))

      ;; combine with apply str to get the equivalent of clojure.string/join
      (is (= (apply str (interpose "-" (sort unordered-a->j))) "a-b-c-d-e-f-g-h-i-j")))

    (testing "filter and some"

      ;; using a one-argument inbuilt function
      (is (= (filter odd? (sort unordered-0->9)) '(1 3 5 7 9)))

      ;; providing our own anonymous function
      (is (= (filter #(not= (mod % 2) 0) (sort unordered-0->9)) '(1 3 5 7 9)))

      ;; providing our own one-argument named function
      (is (= (count (filter cheap? books)) 2))

      ;; some returns the first element for which the predicate is true
      (is (= (some cheap? books) {:title "Dracula" :price 1.99 :genre :horror :rating 7}))

      ;; and nil if there are no elements for which the predicate is true
      (is (= (some extremely-cheap? books) nil))

      ;; filter returns an empty seq if no elements satisfy the predicate
      (is (= (filter extremely-cheap? books) '())))

    (testing "map, comp, for and reduce"

      ;; we can use keywords as functions to combine map lookups with the map function
      (is (= (map :title books) '("Deep Six" "Dracula" "Emma" "2001")))
      (is (= (sort (map :title books)) '("2001" "Deep Six" "Dracula" "Emma")))

      ;; for more complex operations we can provide an anonymous function
      ;; get the price of each book converted to dollars
      (is (= (map #(* 1.2 (:price %)) books) '(16.788 2.388 9.588 12.60)))

      ;; or use comp to combine the functions we want to apply to each elem
      (is (= (map (comp #(* 1.2 %) :price) books) '(16.788 2.388 9.588 12.60)))

      ;; or doing something simple like counting the chars in each book's title
      (is (= (map (comp count :title) books) '(8 7 4 4)))

      ;; for works exactly like map but gives us the option to use a local binding
      ;; useful if you want a more verbose look at what is being done to each elem in the coll
      (is (= (for [book books]
               ((comp count :title) book))
             '(8 7 4 4)))

      ;; reduce can be called with or without an initial value
      ;; omitting the initial value will cause reduce to take the first element of the coll as its initial value
      (is (= (reduce + 0 '(1 2 3)) 6))
      (is (= (reduce + 1 '(1 2 3)) 7))

      ;; something more involved - getting the total price of the books
      (is (= (reduce + (map :price books)) 34.47))

      ;; using reduce to get a single value from a coll that isn't a combination of elems
      ;; here, we use a function to compare the price of each element, ensuring we end up with the highest price
      ;; our reducing function, hi-price, takes two arguments, so we have to provide reduce with an initial value
      (is (= (reduce hi-price 0 books) 13.99)))

    (testing "putting it all together"
      (is (= (three-highest-rated books) "Emma // Dracula // Deep Six")))

    (testing "regex"
      (let [re #"Pride and Prejudice.*"
            title "Pride and Prejudice and Zombies"]
        (is (= (re-matches re title) "Pride and Prejudice and Zombies"))
        (is (nil? (re-matches re "some random string")))

        ;; re-seq for getting a sequence of all the matches
        (is (= (re-seq #"e" title) '("e" "e" "e" "e")))))))
