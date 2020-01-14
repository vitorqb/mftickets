(ns mftickets.domain.templates.properties.types.radio-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.db.templates.properties :as db.templates.properties]
            [mftickets.domain.templates.properties :as properties]
            [mftickets.domain.templates.properties.get :as properties.get]
            [mftickets.domain.templates.properties.create :as properties.create]
            [mftickets.domain.templates.properties.types.radio :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-radio-property!
  (let [options [{:id ::opt1} {:id ::opt1}]
        property-data {:templates.properties.types.radio/options options
                       :value-type @#'sut/value-type}
        created-generic-property {:id 9}
        create-radio-opts-calls (atom [])
        create-radio-opts #(swap! create-radio-opts-calls conj %&)
        get-radio-options! #(do [::get %&])
        result (with-redefs [db.templates.properties/create-radio-options! create-radio-opts
                             db.templates.properties/get-radio-options get-radio-options!]
                 (properties.create/create-type-specific-property!
                  property-data
                  created-generic-property))]

    (testing "Calls create-radio-options!"
      (let [assoc-property-id #(assoc % :property-id (:id created-generic-property))]
        (is (= [[[(assoc-property-id (first options)) (assoc-property-id (second options))]]]
               @create-radio-opts-calls))))

    (testing "Returns created options"
      (is (= {:templates.properties.types.radio/options [::get [created-generic-property]]}
             result)))))

(deftest test-integration-create-and-get-radio-property
  (tu/with-db
    (let [property-data {:id nil
                         :template-section-id 2
                         :name "My Radio Prop"
                         :is-multiple false
                         :value-type @#'sut/value-type
                         :order 0
                         :templates.properties.types.radio/options [{:id nil
                                                                     :property-id nil
                                                                     :value "Foo"}
                                                                    {:id nil
                                                                     :property-id nil
                                                                     :value "Bar"}]}
          result (properties.create/create-property! property-data)
          gotten (properties.get/get-property (:id result))]

      (testing "Create returns the same as get"
        (is (= gotten result)))

      (testing "Returns intenger id"
        (is (int? (:id gotten))))

      (testing "Stores template section id"
        (is (= (:template-section-id property-data) (:template-section-id gotten))))

      (testing "Stores name"
        (is (= (:name property-data) (:name gotten))))

      (testing "Stores is-multiple"
        (is (= (:is-multiple property-data) (:is-multiple gotten))))

      (testing "Stores value-type"
        (is (= (:value-type property-data) (:value-type gotten))))

      (testing "Stores order"
        (is (= (:order property-data) (:order gotten))))

      (testing "Options ids are ints"
        (is (int? (-> result :templates.properties.types.radio/options first :id)))
        (is (int? (-> result :templates.properties.types.radio/options second :id))))

      (testing "Options values are stored"
        (is (= (-> result :templates.properties.types.radio/options first :value) "Foo"))
        (is (= (-> result :templates.properties.types.radio/options second :value) "Bar"))))))
