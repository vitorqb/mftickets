(ns mftickets.routes.services.tickets.validation.common-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.routes.services.tickets.validation.common :as sut]
            [mftickets.test-utils :as tu]))

(defn- get-validation [id]
  (some #(and (= (:validation/id %) id) %) sut/validations))

(deftest test-valid-template

  (let [check-fn (-> ::sut/valid-template get-validation :validation/check-fn)]

    (testing "Invalid if user has no access to template"
      (let [created-by-user-id 1
            template-id 2
            ticket-data {:created-by-user-id created-by-user-id :template-id template-id}
            data {:old-ticket nil :new-ticket ticket-data}
            user-has-acccess? (fn [user template] (and (not= created-by-user-id (:id user))
                                                       (not= template-id (:id template))))]
        (with-redefs [domain.templates/user-has-access-to-template? user-has-acccess?]
          (is (true? (check-fn data))))))

    (testing "Valid if user has no access to template"
      (let [created-by-user-id 1
            template-id 2
            ticket-data {:created-by-user-id created-by-user-id :template-id template-id}
            data {:old-ticket nil :new-ticket ticket-data}
            user-has-acccess? (fn [user template] (and (= created-by-user-id (:id user))
                                                       (= template-id (:id template))))]
        (with-redefs [domain.templates/user-has-access-to-template? user-has-acccess?]
          (is (false? (check-fn data))))))))

(deftest test-all-property-ids-belong-to-template

  (let [check-fn (-> ::sut/all-properties-ids-belong-to-ticket get-validation :validation/check-fn)]

    (testing "Invalid if unknown property-id"
      (let [properties-ids #{1 2 3}
            template {:id 12}
            ticket {:template-id (:id template)
                    :properties-values [{:property-id 1} {:property-id 4}]}]
        (with-redefs [domain.templates.properties/get-properties-ids-set-for-template-id
                      {(:id template) properties-ids}]
          (is (true? (check-fn {:new-ticket ticket}))))))

    (testing "Valid if known property-id"
      (let [properties-ids #{1 2 3}
            template {:id 12}
            ticket {:template-id (:id template)
                    :properties-values [{:property-id 1} {:property-id 2}]}]
        (with-redefs [domain.templates.properties/get-properties-ids-set-for-template-id
                      {(:id template) properties-ids}]
          (is (false? (check-fn {:new-ticket ticket}))))))))
