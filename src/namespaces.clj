(ns namespaces
  (:require
    [clojure.test :refer [deftest testing is]]

    ;; don't do this!
    #_[clojure.string :refer :all]))

(deftest tests
  (testing "current namespace dynamic var"

    ;; this probably won't return the value we are expecting
    ;; this could pass or fail depending on where the test is executed from
    ;; e.g., from IntelliJ/Cursive using context action: (str *ns*) => "cursive.tests.runner"
    ;; e.g., from IntelliJ/Cursive using Tools->REPL->Run Tests in Current NS in REPL: (str *ns*) => "user"

    (println (str *ns*))

    (is (not= (str *ns*) "namespaces")))

  (testing "namespace-qualified keywords"
    (is (= (str ::my-keyword) ":namespaces/my-keyword"))))

(comment

  ;; sending this code to the REPL returns "namespaces"
  (str *ns*)
  )
