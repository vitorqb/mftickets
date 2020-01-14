(ns mftickets.db.templates.properties-test
  (:require [mftickets.db.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

(deftest test-get-generic-properties-for-template

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-generic-properties-for-template {:id 1})))))

  (testing "Empty (unkown id)"
    (tu/with-db
      (is (= [] (sut/get-generic-properties-for-template {:id 999})))))

  (testing "Base"
    (tu/with-db
      (tu/gen-save! tu/template-section {:id 1})
      (let [template-section-property (tu/gen-save! tu/template-section-property
                                                    {:template-section-id 1})]
        (is (= [template-section-property]
               (sut/get-generic-properties-for-template {:id 9})))))))

(deftest test-get-generic-properties-for-templates-ids

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-generic-properties-for-templates-ids (range 0 10))))))

  (testing "Base"
    (tu/with-db
      ;; Adds two template sections for the same template
      (tu/gen-save! tu/template-section {:id 1 :template-id 2})
      (tu/gen-save! tu/template-section {:id 2 :template-id 2})

      ;; An unrelated one
      (tu/gen-save! tu/template-section {:id 777 :template-id 666})

      ;; An unrelated property
      (tu/gen-save! tu/template-section-property {:id 999 :template-section-id 999})

      ;; And three properties for the sections
      (let [property1
            (tu/gen-save! tu/template-section-property {:id 1 :template-section-id 1 :order 1})

            property2
            (tu/gen-save! tu/template-section-property {:id 2 :template-section-id 1 :order 2})

            property3
            (tu/gen-save! tu/template-section-property {:id 3 :template-section-id 2 :order 0})
            
            properties
            [property1 property2 property3]]

        (is (= [] (sut/get-generic-properties-for-templates-ids [])))
        (is (= [] (sut/get-generic-properties-for-templates-ids [888])))
        (is (= [property1 property2 property3]
               (sut/get-generic-properties-for-templates-ids [0 1 2 3])))))))

(deftest test-get-generic-properties-for-section

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-generic-properties-for-section {:id 999})))))

  (testing "Base"
    (tu/with-db
      (let [section (tu/gen-save! tu/template-section)
            property-args {:template-section-id (:id section)}
            property (tu/gen-save! tu/template-section-property property-args)]
        (is (= [property] (sut/get-generic-properties-for-section section)))))))

(deftest test-get-generic-property

  (tu/with-db
    (let [property (tu/gen-save! tu/template-section-property {})]
      (is (= property (sut/get-generic-property (:id property)))))))

(deftest test-delete-property

  (tu/with-db
    (let [section (tu/gen-save! tu/template-section {})
          property (tu/gen-save! tu/template-section-property
                                 {:template-section-id (:id section)})]
      (is (= [property] (sut/get-generic-properties-for-templates-ids [(:template-id section)])))
      (sut/delete-property! property)
      (is (= [] (sut/get-generic-properties-for-templates-ids [(:template-id section)]))))))

(deftest test-update-generic-property

  (tu/with-db
    (let [raw-property {:id 1
                        :template-section-id 2
                        :name "Foo"
                        :is-multiple true
                        :value-type :templates.properties.types/text}

          property
          (tu/gen-save! tu/template-section-property raw-property)

          new-raw-property
          (assoc raw-property
                 :template-section-id 3
                 :name "Bar"
                 :is-multiple false
                 :value-type :templates.properties.types/radio
                 :order 999)

          _
          (sut/update-property-generic-data! new-raw-property)]

      (is (= new-raw-property (sut/get-generic-property 1))))))


(deftest test-create-property

  (tu/with-db
    (let [raw-property {:template-section-id 2
                        :name "Foo"
                        :is-multiple true
                        :value-type :templates.properties.types/text
                        :order 0}

          property
          (sut/create-generic-property! raw-property)]

      (is (= raw-property (dissoc property :id)))
      (is (int? (:id property))))))

(deftest test-create-radio-options!

  (tu/with-db
    (let [property-id 8
          value "Foo"
          options [{:property-id property-id :value value}]]
      (sut/create-radio-options! options)
      (is (= 1 (tu/count! (str "FROM templatePropertiesRadioOptions WHERE propertyId = ?"
                               " and VALUE = ?") property-id value))))))

(deftest test-get-radio-options!

  (tu/with-db
    (let [data {:id 1 :propertyId 2 :value "Foo"}
          _ (tu/insert! :templatePropertiesRadioOptions data)
          [result & rest] (sut/get-radio-options {:id (:propertyId data)})]
      (is (nil? rest))
      (is (= (:id data) (:id result)))
      (is (= (:propertyId data) (:property-id result)))
      (is (= (:value data) (:value result))))))

(deftest test-update-radio-options!

  (tu/with-db
    (let [old-opt1
          (tu/gen-save! tu/template-section-property-radio-option
                        {:id 1 :property-id 3 :value "Foo"})
          
          old-opt2
          (tu/gen-save! tu/template-section-property-radio-option
                        {:id 2 :property-id 3 :value "Bar"})

          property
          (tu/gen-save! tu/template-section-property
                        {:value-type :templates.properties.types/radio
                         :id 3})
          
          _
          (assert (= (sut/get-radio-options property) [old-opt1 old-opt2]))

          new-opt-data
          {:property-id 3 :value "BAz"}

          new-property
          (assoc property :templates.properties.types.radio/options [new-opt-data])
          
          result
          (sut/update-radio-options! new-property)
          
          [new-opt :as new-opts]
          (sut/get-radio-options property)]

      (is (nil? result))
      (is (= 1 (count new-opts)))
      (is (int? (:id new-opt)))
      (is (= (select-keys new-opt [:property-id :value]) new-opt-data)))))
