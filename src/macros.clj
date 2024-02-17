(ns macros)

(defn print-rating [rating]
  (cond
    (pos? rating) (println "Good book!")
    (zero? rating) (println "Totally indifferent.")
    (neg? rating) (println "Run away!")))

(defn arithmetic-if [n pos zero neg]
  (cond
    (pos? n) pos
    (zero? n) zero
    (neg? n) neg))

(defn print-rating-2 [rating]
  (arithmetic-if
    rating
    (println "Good book!")
    (println "Totally indifferent.")
    (println "Run away!")))

(defn arithmetic-if-2 [n pos-f zero-f neg-f]
  (cond
    (pos? n) (pos-f)
    (zero? n) (zero-f)
    (neg? n) (neg-f)))

(defn print-rating-3 [rating]
  (arithmetic-if-2
    rating
    #(println "Good book!")
    #(println "Totally indifferent.")
    #(println "Run away!")))

(defn arithmetic-if->cond [n pos zero neg]
  (list 'cond
        (list 'pos? n) pos
        (list 'zero? n) zero
        :else neg))

(defmacro arithmetic-if-macro [n pos zero neg]
  (list 'cond
        (list 'pos? n) pos
        (list 'zero? n) zero
        :else neg))

(defn print-rating-using-macro [rating]
  (arithmetic-if-macro rating (println :loved-it) (println :meh) (println :hated-it)))

(defmacro arithmetic-if-macro-with-syntax-quoting [n pos zero neg]
  `(cond
     (pos? ~n) ~pos
     (zero? ~n) ~zero
     :else ~neg))

(defn print-rating-using-macro-with-syntax-quoting [rating]
  (arithmetic-if-macro-with-syntax-quoting rating (println :loved-it) (println :meh) (println :hated-it)))

(defmacro our-defn [name args & body]
  `(def ~name (fn ~args ~@body)))

(our-defn add2 [a b]
          (+ a b))

(defmacro mark-the-times []
  (println "This is code that runs when the macro is expanded.")
  `(println "This is the generated code"))

(defn use-the-macro []
  (mark-the-times))

(defmacro describe-it [it]
  `(let [value# ~it]
     (cond
       (list? value#) :a-list
       (vector? value#) :a-vector
       (number? value#) :a-number
       :else :no-idea)))

(comment

  (print-rating 1)
  (print-rating 0)
  (print-rating -1)

  (arithmetic-if 0 :great :meh :boring)

  (print-rating-2 10)

  (print-rating-3 -10)

  (arithmetic-if->cond 'rating
                       '(println "Good book!")
                       '(println "Totally indifferent.")
                       '(println "Run away!"))

  (arithmetic-if-macro 'rating
                       '(println "Good book!")
                       '(println "Totally indifferent.")
                       '(println "Run away!"))

  (print-rating-using-macro 42)

  (def n 100)
  (def pos "It's positive!")
  (def zero "It's zero!")
  (def neg "It's negative!")

  `(cond
     (pos? ~n) ~pos
     (zero? ~n ~zero)
     :else ~neg)

  (print-rating-using-macro-with-syntax-quoting 42)

  (add2 2 2)

  (use-the-macro)

  (macroexpand-1 `(println "This is the generated code"))
  (macroexpand-1 `(mark-the-times))
  (macroexpand-1 `(arithmetic-if-macro-with-syntax-quoting 100 :pos :zero :neg))

  (describe-it 37)
  (map describe-it [10 "a string" [1 2 3]])
  )
