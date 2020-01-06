(ns mftickets.domain.tickets.properties-values-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.tickets.properties-values :as sut]
            [mftickets.domain.tickets.properties-values.get
             :as
             tickets.properties-values.get]
            [mftickets.test-utils :as tu]))

(deftest test-get-properties-values

  (let [ticket {:id 121}
        property1 {:id 1}
        property-val1 {:id 3}
        property2 {:id 2}
        properties-values {{:property property1 :ticket ticket} property-val1
                           {:property property2 :ticket ticket} nil}
        m-get-property-value (fn [x] (get properties-values x))
        result (with-redefs [tickets.properties-values.get/get-property-value
                             m-get-property-value]
                 (doall (sut/get-properties-values [property1 property2])))]

    (testing "Returns property value for property"
      (is (= property-val1 (first result))))

    (testing "Does not return property values which are nil"
      (is (= 1 (count result))))))

(deftest test-get-properties-values

  (let [ticket {:id 99}
        
        property1
        {:id 1}

        property-val1
        {:id 3}

        property2
        {:id 2}

        m-get-property-value
        (fn [{ticket* :ticket property* :property}]
          (when (= ticket* ticket)
            (condp = property*
              property1 property-val1
              property2 nil)))

        opts
        {:ticket ticket :properties [property1 property2]}
        
        result (with-redefs [tickets.properties-values.get/get-property-value
                             m-get-property-value]
                 (doall (sut/get-properties-values opts)))]

    (testing "Returns property value for property"
      (is (= property-val1 (first result))))

    (testing "Does not return property values which are nil"
      (is (= 1 (count result))))))
