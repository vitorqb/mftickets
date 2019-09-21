(ns mftickets.routes.services.templates-test
  (:require
   [com.rpl.specter :as s]
   [mftickets.routes.services.templates :as sut]
   [clojure.test :as t :refer [is are deftest testing use-fixtures]]
   [mftickets.test-utils :as tu]
   [mftickets.db.core :as db.core]
   [mftickets.db.prefill :as db.prefill]
   [mftickets.handler :refer [app]]
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [mftickets.domain.users :as domain.users]
   [mftickets.domain.projects :as domain.projects]
   [mftickets.middleware.auth :as middleware.auth]
   [mftickets.utils.kw :as utils.kw]
   [ring.mock.request :as mock.request]))

(deftest test-user-has-access-to-template?

  (let [user-has-access-to-template? #'sut/user-has-access-to-template?]

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4 1})]
        (is (true? (user-has-access-to-template? nil nil)))))

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4})]
        (is (false? (user-has-access-to-template? nil nil)))))))

(deftest test-wrap-user-has-access-to-template??

  (let [wrap-user-has-access-to-template? #'sut/wrap-user-has-access-to-template?]

    (testing "No template -> calls handler."
      (let [handler (wrap-user-has-access-to-template? identity)
            request {::foo ::bar}]
        (is (= request (handler request)))))

    (testing "Template and user not allowed."
      (with-redefs [sut/user-has-access-to-template? (constantly false)]
        (let [handler (wrap-user-has-access-to-template? identity)
              request {::sut/template {:id 1}}
              response (handler request)]
          (is (= {:status 404} response)))))

    (testing "Template id and user has access."
      (with-redefs [sut/user-has-access-to-template? (fn [user template]
                                           (and (= user ::user) (= template ::template)))]
        (let [request {:mftickets.auth/user ::user ::sut/template ::template}
              handler (wrap-user-has-access-to-template? #(hash-map ::param %))
              response (handler request)]
          (is (= {::param request} response)))))))

(deftest test-user-has-access-to-project?

  (let [user-has-access-to-project? #'sut/user-has-access-to-project?
        user-id 999
        user {:id user-id}]

    (with-redefs [domain.users/get-projects-ids-for-user #(if (= (:id %) user-id) #{1 2})]

      (testing "True"
        (is (true? (user-has-access-to-project? user {:id 1})))
        (is (true? (user-has-access-to-project? user {:id 2}))))

      (testing "False"
        (is (false? (user-has-access-to-project? user {:id 3})))
        (is (false? (user-has-access-to-project? user {:id 4})))))))

(deftest test-wrap-user-has-access-to-project?

  (let [wrap-user-has-access-to-project? #'sut/wrap-user-has-access-to-project?
        user {:id 1}
        project {:id 2}]
    (with-redefs [sut/user-has-access-to-project?
                  (fn [user* project*] (and (= user user*) (= project project*)))]

      (testing "True"
        (let [handler (wrap-user-has-access-to-project? identity)
              request {:mftickets.auth/user user ::sut/project project}
              response (handler request)]
          (is (= response request))))

      (testing "False"
        (let [handler (wrap-user-has-access-to-project? identity)
              request {:mftickets.auth/user {:id 3} ::sut/project project}
              response (handler request)]
          (is (= {:status 400 :body {:message sut/err-msg-invalid-project-id}}
                 response)))))))

(deftest test-assoc-sections

  (let [assoc-sections #'sut/assoc-sections]

    (testing "Base"
      (let [sections [{:id 1} {:id 2}]
            sections-getter (constantly sections)]
        (is (= {:sections sections}
               (assoc-sections {} sections-getter)))))))

(deftest test-assoc-properties

  (let [assoc-properties #'sut/assoc-properties]

    (testing "Base"
      (let [properties [{:id 1 :template-section-id 10}
                        {:id 2 :template-section-id 10}
                        {:id 3 :template-section-id 11}]
            sections [{:id 10} {:id 11} {:id 12}]
            template {:sections sections}
            property-getter (constantly properties)]
        (is (= {:sections [{:id 10
                            :properties [{:id 2 :template-section-id 10}
                                         {:id 1 :template-section-id 10}]}
                           {:id 11
                            :properties [{:id 3 :template-section-id 11}]}
                           {:id 12}]}
               (assoc-properties template property-getter)))))))

(deftest test-get-template

  (let [wrap-get-template #'sut/wrap-get-template
        wrap-user-has-access-to-template? #'sut/wrap-user-has-access-to-template?]

    (testing "Non-existing template"
      (with-redefs [domain.templates/get-raw-template (constantly nil)]
        (is (= {:status 404}
               (sut/handle-get {:parameters {:path {:id 1}}})))))

    (testing "Existing template"
      (with-redefs [domain.templates/get-raw-template
                    #(hash-map :id %)

                    domain.templates.sections/get-sections-for-template
                    (constantly [{:id 9}])

                    domain.templates.properties/get-properties-for-template
                    (constantly [{:id 8 :template-section-id 9}])]

        (let [handler (-> sut/handle-get wrap-get-template)
              response (handler {:parameters {:path {:id 9}}})]
          (are [ks f] (f (get-in response ks ::nf))
            [:status]   #(= 200 %)
            [:body :id] #(= 9 %)
            [:body :sections] #(= [{:id 9 :properties [{:id 8 :template-section-id 9}]}] %)))))))

(deftest test-wrap-get-template

  (let [wrap-get-template #'sut/wrap-get-template]

    (testing "Missing template-id"
      (let [request {}
            handler (wrap-get-template identity)
            result (handler request)]
        (is (= request result))))

    (testing "template-id mapping to no template."
      (with-redefs [sut/get-template #(if (= % 9) nil ::wrong-argument)]
        (let [request {:parameters {:path {:id 9}}}
              handler (wrap-get-template identity)
              result (handler request)]
          (is (= request result)))))

    (testing "template-id mapping to template"
      (with-redefs [sut/get-template #(when (= % 9) ::template)]
        (let [request {:parameters {:path {:id 9}}}
              handler (wrap-get-template identity)
              result (handler request)]
          (is (= (assoc request ::sut/template ::template) result)))))))

(deftest test-wrap-get-project

  (let [wrap-get-project #'sut/wrap-get-project]

    (testing "Project not found"
      (with-redefs [domain.projects/get-project #(when-not (= % 1) ::found)]
        (let [handler (wrap-get-project identity)
              request {:parameters {:query {:project-id 1}}}
              response (handler {:parameters {:query {:project-id 1}}})]
          (is (= {:status 400 :body {:message sut/err-msg-invalid-project-id}} response)))))

    (testing "Project found"
      (with-redefs [domain.projects/get-project #(when (= % 1) ::project)]
        (let [handler (wrap-get-project identity)
              request {:parameters {:query {:project-id 1}}}
              response (handler request)]
          (is (= (assoc request ::sut/project ::project) response)))))))

(deftest test-handle-get

  (testing "Inexistant template returns 404"
    (is (= {:status 404} (sut/handle-get {}))))

  (testing "Existing template is returned"
    (let [template-id 999]
      (is (= {:status 200 :body ::template}
             (sut/handle-get {::sut/template ::template}))))))

(deftest test-app-integration-get-template

  (testing "Existing template"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]
          (with-redefs [sut/user-has-access-to-template? (fn [user* _] (= user* user))]
            (tu/gen-save! tu/template
                          {:id 1 :project-id 1 :creation-date "2019-09-14T19:08:45"})
            (tu/gen-save! tu/template-section {:id 1 :template-id 1})
            (tu/gen-save! tu/template-section-property {:id 1 :template-section-id 1})
            (tu/gen-save! tu/template-section-property {:id 2 :template-section-id 1})
            (let [req (-> (mock.request/request :get "/api/templates/1")
                          (tu/auth-header token))
                  resp ((app) req)
                  body (tu/decode-response-body resp)]

              (testing "Returns 200"
                (is (= 200 (:status resp))))

              (testing "Returns body with keys"
                (are [ks f] (f (get-in body ks ::nf))
                  [:id] #(= 1 %)
                  [:project-id] #(= 1 %)
                  [:creation-date] #(= "2019-09-14T19:08:45" %)
                  [:sections] #(= (count %) 1)
                  [:sections 0 :properties] #(= (count %) 2))))))))))

(deftest test-app-integration-get-template-list

  (testing "Base integration"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]

          ;; Adds a project
          (tu/gen-save! tu/project {:id 99})

          ;; Assign to user
          (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 99})
          
          (let [raw-project-template
                (tu/gen-save! tu/template {:id 1 :project-id 99})

                project-template-section
                (tu/gen-save! tu/template-section {:id 2 :template-id 1})

                project-template-section-property
                (tu/gen-save! tu/template-section-property {:template-section-id 2})

                project-template
                (-> raw-project-template
                    (#'sut/assoc-sections (constantly [project-template-section]))
                    (#'sut/assoc-properties (constantly [project-template-section-property])))

                other-templates
                [(tu/gen-save! tu/template {:id 2 :project-id 100})]

                request
                (-> (mock.request/request :get "/api/templates")
                    (mock.request/query-string {:project-id 99})
                    (tu/auth-header token))

                resp
                ((app) request)

                body
                (tu/decode-response-body resp)]

            (testing "Returns 200"
              (is (= 200 (:status resp))))

            (testing "Returns list of templates"
              ;; keyword is returned as string
              (let [project-template*
                    (s/transform
                     [:sections s/ALL :properties s/ALL :value-type]
                     utils.kw/full-name
                     project-template)]
                (is (= [project-template*] body))))))))))

(deftest test-get-project-templates
  (let [get-project-templates #'sut/get-project-templates]

    (testing "Base"
      (let [project {:id 1}
            raw-template {:id 2}
            sections [{:id 3}]
            properties [{:id 4 :template-section-id 3}]]
        (with-redefs [domain.templates/get-raw-templates-for-project
                      #(when (= % project) [raw-template])

                      domain.templates.properties/properties-getter
                      #(when (= % [raw-template]) (constantly properties))

                      domain.templates.sections/sections-getter
                      #(when (= % [raw-template]) (constantly sections))]

          (is (= [{:id 2 :sections [{:id 3 :properties properties}]}]
                 (get-project-templates project))))))))

(deftest test-handle-get-project-templates
  (let [templates [::foo]
        project ::project
        templates [::template1 ::template2]]
    (with-redefs [sut/get-project-templates #(when (= % project) templates)]
      (is (= {:status 200 :body templates}
             (sut/handle-get-project-templates {::sut/project project}))))))
