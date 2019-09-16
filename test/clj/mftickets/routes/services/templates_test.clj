(ns mftickets.routes.services.templates-test
  (:require
   [mftickets.routes.services.templates :as sut]
   [clojure.test :as t :refer [is are deftest testing use-fixtures]]
   [mftickets.test-utils :as test-utils]
   [mftickets.db.core :as db.core]
   [mftickets.db.prefill :as db.prefill]
   [mftickets.handler :refer [app]]
   [mftickets.domain.templates :as domain.templates]
   [mftickets.domain.templates.sections :as domain.templates.sections]
   [mftickets.domain.templates.properties :as domain.templates.properties]
   [mftickets.domain.users :as domain.users]
   [mftickets.middleware.auth :as middleware.auth]
   [ring.mock.request :as mock.request]))

(deftest test-user-has-access?

  (let [user-has-access? #'sut/user-has-access?]

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4 1})]
        (is (true? (user-has-access? nil nil)))))

    (testing "True"
      (with-redefs [domain.users/get-projects-ids-for-user (constantly #{1 2})
                    domain.templates/get-projects-ids-for-template (constantly #{3 4})]
        (is (false? (user-has-access? nil nil)))))))

(deftest test-wrap-has-access?

  (let [wrap-user-has-access? #'sut/wrap-user-has-access?]

    (testing "No template -> calls handler."
      (let [handler (wrap-user-has-access? identity)
            request {::foo ::bar}]
        (is (= request (handler request)))))

    (testing "Template and user not allowed."
      (with-redefs [sut/user-has-access? (constantly false)]
        (let [handler (wrap-user-has-access? identity)
              request {::sut/template {:id 1}}
              response (handler request)]
          (is (= {:status 404} response)))))

    (testing "Template id and user has access."
      (with-redefs [sut/user-has-access? (fn [user template]
                                           (and (= user ::user) (= template ::template)))]
        (let [request {:mftickets.auth/user ::user ::sut/template ::template}
              handler (wrap-user-has-access? #(hash-map ::param %))
              response (handler request)]
          (is (= {::param request} response)))))))

(deftest test-assoc-sections

  (let [assoc-sections #'sut/assoc-sections]

    (testing "Base"
      (let [sections [{:id 1} {:id 2}]]
        (with-redefs [domain.templates.sections/get-sections-for-template (constantly sections)]
          (is (= {:sections sections}
                 (assoc-sections {}))))))))

(deftest test-assoc-properties

  (let [assoc-properties #'sut/assoc-properties]

    (testing "Base"
      (let [properties [{:id 1 :template-section-id 10}
                        {:id 2 :template-section-id 10}
                        {:id 3 :template-section-id 11}]
            sections [{:id 10} {:id 11} {:id 12}]
            template {:sections sections}]
        (with-redefs [domain.templates.properties/get-properties-for-template
                      (constantly properties)]
          (is (= {:sections [{:id 10
                              :properties [{:id 2 :template-section-id 10}
                                           {:id 1 :template-section-id 10}]}
                             {:id 11
                              :properties [{:id 3 :template-section-id 11}]}
                             {:id 12}]}
                 (assoc-properties template))))))))

(deftest test-get-template

  (let [wrap-get-template #'sut/wrap-get-template
        wrap-user-has-access? #'sut/wrap-user-has-access?]

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

(deftest test-handle-get

  (testing "Inexistant template returns 404"
    (is (= {:status 404} (sut/handle-get {}))))

  (testing "Existing template is returned"
    (let [template-id 999]
      (is (= {:status 200 :body ::template}
             (sut/handle-get {::sut/template ::template}))))))

(deftest test-app-integration-get-template

  (testing "Existing template"
    (test-utils/with-app
      (test-utils/with-db
        (test-utils/with-user-and-token [user token]
          (with-redefs [sut/user-has-access? (fn [user* _] (= user* user))]
            (db.prefill/run-prefills! db.core/*db*)
            (let [req (-> (mock.request/request :get "/api/templates/1")
                          (test-utils/auth-header token))
                  resp ((app) req)
                  body (test-utils/decode-response-body resp)]

              (testing "Returns 200"
                (is (= 200 (:status resp))))

              (testing "Returns body with keys"
                (are [ks f] (f (get-in body ks ::nf))
                  [:id] #(= 1 %)
                  [:project-id] #(= 1 %)
                  [:creation-date] #(= "2019-09-14T19:08:45" %)
                  [:sections] #(= (count %) 1)
                  [:sections 0 :properties] #(= (count %) 5))))))))))
