(ns mftickets.db.templates.properties-test
  (:require [mftickets.db.templates.properties :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]))

(deftest test-get-properties-for-template

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-properties-for-template {:id 1})))))

  (testing "Empty (unkown id)"
    (tu/with-db
      (is (= [] (sut/get-properties-for-template {:id 999})))))

  (testing "Base"
    (tu/with-db
      (tu/gen-save! tu/template-section {:id 1})
      (let [template-section-property (tu/gen-save! tu/template-section-property
                                                    {:template-section-id 1})]
        (is (= [template-section-property]
               (sut/get-properties-for-template {:id 9})))))))

(deftest test-get-properties-for-templates-ids

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-properties-for-templates-ids (range 0 10))))))

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
      (let [properties
            [(tu/gen-save! tu/template-section-property {:id 1 :template-section-id 1})
             (tu/gen-save! tu/template-section-property {:id 2 :template-section-id 1})
             (tu/gen-save! tu/template-section-property {:id 3 :template-section-id 2})]]

        (is (= [] (sut/get-properties-for-templates-ids [])))
        (is (= [] (sut/get-properties-for-templates-ids [888])))
        (is (= properties (sut/get-properties-for-templates-ids [0 1 2 3])))))))

(deftest test-get-properties-for-section

  (testing "Empty"
    (tu/with-db
      (is (= [] (sut/get-properties-for-section {:id 999})))))

  (testing "Base"
    (tu/with-db
      (let [section (tu/gen-save! tu/template-section)
            property-args {:template-section-id (:id section)}
            property (tu/gen-save! tu/template-section-property property-args)]
        (is (= [property] (sut/get-properties-for-section section)))))))

(deftest test-get-property

  (tu/with-db
    (let [property (tu/gen-save! tu/template-section-property {})]
      (is (= property (sut/get-property (:id property)))))))

(deftest test-delete-property

  (tu/with-db
    (let [section (tu/gen-save! tu/template-section {})
          property (tu/gen-save! tu/template-section-property
                                 {:template-section-id (:id section)})]
      (is (= [property] (sut/get-properties-for-templates-ids [(:template-id section)])))
      (sut/delete-property! property)
      (is (= [] (sut/get-properties-for-templates-ids [(:template-id section)]))))))

(deftest test-update-property

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
                 :value-type :templates.properties.types/radio)

          _
          (sut/update-property! new-raw-property)]

      (is (= new-raw-property (sut/get-property 1))))))


(deftest test-create-property

  (tu/with-db
    (let [raw-property {:template-section-id 2
                        :name "Foo"
                        :is-multiple true
                        :value-type :templates.properties.types/text}

          property
          (sut/create-property! raw-property)]

      (is (= raw-property (dissoc property :id)))
      (is (int? (:id property))))))
