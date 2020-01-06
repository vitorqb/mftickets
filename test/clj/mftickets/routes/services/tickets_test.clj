(ns mftickets.routes.services.tickets-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.templates.properties :as domain.properties]
            [mftickets.domain.tickets :as domain.tickets]
            [mftickets.handler :refer [app]]
            [mftickets.http.responses :as http.responses]
            [mftickets.inject :as inject]
            [mftickets.routes.services.tickets :as sut]
            [mftickets.routes.services.tickets.validation.common
             :as
             tickets.validation.common]
            [mftickets.test-utils :as tu]
            [mftickets.utils.kw :as kw]
            [ring.mock.request :as mock.request]))

(use-fixtures :once tu/common-fixture)

(deftest test-create-ticket!

  (let [ticket-data {:id nil
                     :template-id 1
                     :created-at nil
                     :created-by-user-id nil
                     :properties-values [{}]}
        user {:id 2}
        request {:mftickets.auth/user user :parameters {:body ticket-data}}
        properties [{:id 1} {:id 2}]]

    (testing "Calls create-ticket! after associng user id and opts"
      (with-redefs [domain.tickets/create-ticket! (fn [x y] [::create-ticket x y])
                    sut/get-properties-for-ticket-data (constantly properties)]
        (is (= [::create-ticket
                (assoc ticket-data :created-by-user-id (:id user))
                {:properties properties}]
               (#'sut/create-ticket! request)))))))

(deftest test-handle-create

  (testing "Fails if validation fails"
    (let [new-ticket {:id nil}
          request {{:body new-ticket} :parameters}
          error [::foo "bar"]]
      (with-redefs [sut/validate-ticket-create (constantly error)]
        (is (= (http.responses/validation-error error)
               (#'sut/handle-create request))))))

  (testing "Calls create ticket if validation passess"
    (let [new-ticket {:id nil}
          request {{:body new-ticket} :parameters}]
      (with-redefs [sut/validate-ticket-create (constantly :validation/success)
                    sut/create-ticket! (fn [x] [::create-ticket x])]
        (is (= {:status 200 :body [::create-ticket request]}
               (#'sut/handle-create request)))))))

(deftest test-create-integration

  (testing "Valid ticket data"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token project]

          (let [template
                (tu/gen-save! tu/template {:project-id (:id project)})

                section1
                (tu/gen-save! tu/template-section {:id 9182 :template-id (:id template)})

                section2
                (tu/gen-save! tu/template-section {:id 1231 :template-id (:id template)})

                property1
                (tu/gen-save! tu/template-section-property
                              {:id 161672872178
                               :template-section-id (:id section1)
                               :value-type :templates.properties.types/text})

                property2
                (tu/gen-save! tu/template-section-property
                              {:id 782178278126
                               :template-section-id (:id section2)
                               :value-type :templates.properties.types/text})

                ticket-data
                {:id nil
                 :template-id (:id template)
                 :created-at nil
                 :created-by-user-id nil
                 :properties-values
                 [{:id nil
                   :ticket-id nil
                   :property-id (:id property1)
                   :templates.properties.types.text/value "This is a text"}
                  {:id nil
                   :ticket-id nil
                   :property-id (:id property2)
                   :templates.properties.types.text/value "This is another text"}]}

                request
                (-> (mock.request/request :post "/api/tickets")
                    (mock.request/json-body ticket-data)
                    (tu/auth-header token))

                response
                ((app) request)

                body
                (tu/decode-response-body response)]

            (testing "Returns 200"
              (is (= 200 (:status response))))

            (testing "Returns the created ticket"
              (is (int? (:id body)))
              (is (= (:template-id ticket-data) (:template-id body)))
              (is (string? (:created-at body)))
              (is (= (:created-by-user-id body) (:id user)))
              (is (= 2 (-> body :properties-values count)))

              (is (int? (-> body :properties-values first :id)))
              (is (= (:id property1) (-> body :properties-values first :property-id)))
              (is (= "This is a text" (-> body :properties-values first :templates.properties.types.text/value)))

              (is (int? (-> body :properties-values second :id)))
              (is (= (:id property2) (-> body :properties-values second :property-id)))
              (is (= "This is another text" (-> body :properties-values second :templates.properties.types.text/value)))))))))

  (testing "Invalid Ticket data"
    (testing "Invalid template id returns error"

      (tu/with-db
        (tu/with-app
          (tu/with-user-and-token [user token project]
            (let [ ;; A template user has no access to!
                  template
                  (tu/gen-save! tu/template {:project-id (-> project :id inc)})

                  ticket-data
                  {:id nil
                   :template-id (:id template)
                   :created-ai nil
                   :created-by-user-id nil
                   :properties-values []}

                  request
                  (-> (mock.request/request :post "/api/tickets")
                      (mock.request/json-body ticket-data)
                      (tu/auth-header token))

                  response
                  ((app) request)

                  body
                  (tu/decode-response-body response)]

              (testing "Returns 400"
                (is (= 400 (:status response))))

              (testing "Returns error message"
                (is (= (kw/full-name ::tickets.validation.common/valid-template)
                       (:error-key body)))
                (is (= "Invalid template id!" (:error-message body)))))))))))
