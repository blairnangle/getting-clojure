(ns destructuring
  (:require [clojure.test :refer [deftest testing is]]))

(def artists-vec ["Blunt" "Heaney" "St Clair" "Wolfe" "Frahm"])
(def artist-pairs-vec [["Blunt" "Heaney"] ["St Clair" "Wolfe"] ["Frahm" "Page"]])

(def artists-list '("Blunt" "Heaney" "St Clair" "Wolfe" "Frahm"))

(defn artist-description
  "Expects a two-argument collection where the first argument is a novelist and the second is a poet."
  [[novelist poet]]
  (str "The novelist is " novelist " and the poet is " poet))

(defn possibly-shout-artist-description
  "Expects a two-argument collection where the first argument is a novelist and the second is a poet,
  and a boolean indicated if the description should be shouted."
  [[novelist poet] shout?]
  (let [desc (str "The novelist is " novelist " and the poet is " poet)]
    (if shout?
      (.toUpperCase desc)
      desc)))

(def artists-map {:actor    "Blunt"
                  :poet     "Heaney"
                  :novelist "St Clair"
                  :singer   "Wolfe"
                  :composer "Frahm"})

(defn use-the-original-too [{{:keys [father mother]} :parents name :name :as author}]
  (println "The author's name is" name)
  (println "The author's father is" father)
  (println "The author's mother is" mother)
  author)

(defn use-or [{:keys [name nationality] :or {nationality "English"} :as author}]
  (println "The author's name is" name)
  (println "nationality will always be English unless our author map contains a value for it")
  (println "The author's nationality is" nationality)
  nationality)

(deftest tests

  ;; we use the order of the artists vector to our advantage
  (testing "sequential"

    ;; note that we do not need to destructure the entire vector
    (let [[actor poet novelist singer] artists-vec]
      (is (= actor "Blunt"))
      (is (= poet "Heaney"))
      (is (= novelist "St Clair"))
      (is (= singer "Wolfe")))

    ;; we can ignore some elements if we choose to
    (let [[_ _ _ _ composer] artists-vec]
      (is (= composer "Frahm")))

    ;; we can do nested sequential destructuring by mirroring the shape of the
    (let [[[actor poet] [novelist singer] [composer guitarist]] artist-pairs-vec]
      (is (= actor "Blunt"))
      (is (= poet "Heaney"))
      (is (= novelist "St Clair"))
      (is (= singer "Wolfe"))
      (is (= composer "Frahm"))
      (is (= guitarist "Page")))

    ;; also works with lists
    (let [[_ poet] artists-list]
      (is (= poet "Heaney"))

      ;; and strings -> characters
      (let [[c1 c2 c3 c4] poet]
        (is (= c1 \H))
        (is (= c2 \e))
        (is (= c3 \a))
        (is (= c4 \n))))

    ;; also works in the argument list of a function
    (is (= (artist-description [:austen :dickinson]) "The novelist is :austen and the poet is :dickinson"))

    ;; we can also combine destructured and regular arguments in a function's arg list
    (is (= (possibly-shout-artist-description [:austen :dickinson] false) "The novelist is :austen and the poet is :dickinson"))
    (is (= (possibly-shout-artist-description [:austen :dickinson] true) "THE NOVELIST IS :AUSTEN AND THE POET IS :DICKINSON")))

  (testing "associative"
    (let [{actor    :actor
           poet     :poet
           novelist :novelist
           singer   :singer
           composer :composer} artists-map]
      (is (= actor "Blunt"))
      (is (= poet "Heaney"))
      (is (= novelist "St Clair"))
      (is (= singer "Wolfe"))
      (is (= composer "Frahm")))

    ;; it also works for nested maps
    (let [austen {:name    "Jane Austen"
                  :parents {:father "George"
                            :mother "Cassandra"}
                  :dates   {:born 1775
                            :died 1817}}]
      (let [{{dad :father mum :mother} :parents} austen]
        (is (= dad "George"))
        (is (= mum "Cassandra")))

      ;; we can omit the symbols when destructuring associatively if we are OK with our new vars being named the same thing as the map's keys
      (let [{:keys [name]} austen]
        (is (= name "Jane Austen")))

      ;; we can even do this within our nested map - but notice that the outer vars have gone back to [symbol :keyword]
      (let [{{:keys [father mother]} :parents name :name} austen]
        (is (= name "Jane Austen"))
        (is (= father "George"))
        (is (= mother "Cassandra")))

      ;; using :as we hold onto the value of the original argument, not just those keys that we chose to destructure
      (is (= (use-the-original-too austen) austen))

      ;; we can use :or to set defaults
      (is (= (use-or austen) "English"))

      ;; but the :or default will be overridden if we pass something in that gets destructured
      (is (= (use-or (assoc austen :nationality "British")) "British")))))
