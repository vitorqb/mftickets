(ns mftickets.validation.core-test
  (:require [mftickets.validation.core :as sut]
            [clojure.test :as t :refer [are deftest is testing use-fixtures]]))

(deftest test-validate

  (testing "Empty"
    (let [validations []
          data {}]
      (is (= :validation/success (sut/validate validations data)))))

  (testing "One failed validation"
    (let [id :foo
          message "Foo"
          check-fn (constantly true)
          validations [#:validation{:id id :message message :check-fn check-fn}]
          data {}]
      (is (= [:foo "Foo"]
             (sut/validate validations data)))))

  (testing "One passed validation"
    (let [id :foo
          message "Foo"
          check-fn (constantly false)
          validations [#:validation{:id id :message message :check-fn check-fn}]
          data {}]
      (is (= :validation/success (sut/validate validations data)))))

  (testing "Passes the arguments to the check-fn"
    (let [id :foo
          message "foo"
          data {::foo 1}
          check-fn (fn [x] (= data x))
          validations [#:validation{:id id :message message :check-fn check-fn}]]
      (is (= [id message] (sut/validate validations data)))))

  (testing "Stops on first validation"
    (let [id1 :id1
          message1 "message1"
          check-fn1 (constantly false)
          validation1 #:validation{:id id1 :message message1 :check-fn check-fn1}

          id2 :id2
          message2 "message2"
          check-fn2 (constantly true)
          validation2 #:validation{:id id2 :message message2 :check-fn check-fn2}
          
          id3 :id3
          message3 "message3"
          check-fn3 (constantly true)
          validation3 #:validation{:id id3 :message message3 :check-fn check-fn3}

          validations [validation1 validation2 validation3]]
      (is (= [id2 message2] (sut/validate validations {}))))))

(deftest test-if-let-err

  (testing "Else branch"
    (is (= ::bar
           (sut/if-let-err [e ((constantly :validation/success))]
             ::foo
             ::bar))))

  (testing "If branch"
    (is (= [::result [::foo ::bar]]
           (sut/if-let-err [e ((constantly [::foo ::bar]))]
             [::result e]
             ::bar)))))
