;; ---------------------------------------------------------
;; com.arcos.gwap.-test
;;
;; Example unit tests for com.arcos.gwap
;;
;; - `deftest` - test a specific function
;; - `testing` logically group assertions within a function test
;; - `is` assertion:  expected value then function call
;; ---------------------------------------------------------


(ns com.arcos.gwap-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.arcos.gwap :as gwap]))

(deftest application-test
  (testing "TODO: Start with a failing test, make it pass, then refactor"

    ;; TODO: fix greet function to pass test
    (is (= "com.arcos application developed by the secret engineering team"
           (gwap/greet)))

    ;; TODO: fix test by calling greet with {:team-name "Practicalli Engineering"}
    (is (= (gwap/greet "Practicalli Engineering")
           "com.arcos service developed by the Practicalli Engineering team"))))
