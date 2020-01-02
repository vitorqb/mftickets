(ns mftickets.db.prefill-test
  (:require [mftickets.db.prefill :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [clojure.java.jdbc :as jdbc]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-parse-args

  (let [parse-args #'sut/parse-args]

    (testing "Empty"
      (let [args []
            opts {:db ::db}
            result (parse-args args opts)]
        (is (= [] result))))

    (testing "One Long"
      (let [args [:tableName {:arg1 :value1 :arg2 :value2}]
            opts {:db ::db}
            result (parse-args args opts)]
        (is (= [[jdbc/insert! ::db :tableName {:arg1 :value1 :arg2 :value2}]]
               result))))

    (testing "Two Long"
      (let [args [:table1 {:arg1 :value1}
                  :table2 {:arg2 :value2}]
            opts {:db ::db}
            result (parse-args args opts)]
        (is (= [[jdbc/insert! ::db :table1 {:arg1 :value1}]
                [jdbc/insert! ::db :table2 {:arg2 :value2}]]
               result))))))
