(ns mftickets.routes.services.templates-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [com.rpl.specter :as s]
            [mftickets.db.core :as db.core]
            [mftickets.db.prefill :as db.prefill]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.users :as domain.users]
            [mftickets.handler :refer [app]]
            [mftickets.middleware.auth :as middleware.auth]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.middleware.pagination :as middleware.pagination] 
            [mftickets.routes.services.templates :as sut]
            [mftickets.test-utils :as tu]
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

                {:keys [page-number page-size total-items-count items] :as body}
                (tu/decode-response-body resp)]

            (testing "Returns 200"
              (is (= 200 (:status resp))))

            (testing "Returns paginated response"
              (is (= middleware.pagination/default-page-number page-number))
              (is (= middleware.pagination/default-page-size page-size))
              (is (= 1 total-items-count)))

            (testing "Returns list of templates inside :items"
              ;; keyword is returned as string
              (let [project-template*
                    (s/transform
                     [:sections s/ALL :properties s/ALL :value-type]
                     utils.kw/full-name
                     project-template)]
                (is (= [project-template*] items))))

            ;; Now adds another template
            (tu/gen-save! tu/template {:id 3 :project-id 99})

            (testing "Pagination with more than one items"
              (let [request
                    (-> (mock.request/request :get "/api/templates")
                        (mock.request/query-string {:project-id 99
                                                    :pageNumber 1
                                                    :pageSize 1})
                        (tu/auth-header token))

                    resp
                    ((app) request)

                    {:keys [page-number page-size total-items-count items] :as body}
                    (tu/decode-response-body resp)]
                (is (= 1 page-number))
                (is (= 1 page-size))
                (is (= 1 (count items)))
                (is (= 2 total-items-count))))))))))

(deftest test-handle-get-project-templates

  (testing "Base"
    (let [pagination-data
          #::middleware.pagination{:page-number 2 :page-size 3}
          
          project
          {:id 9}

          request
          {::middleware.pagination/page-number 2
           ::middleware.pagination/page-size 3
           ::middleware.context/project project}

          templates
          [{:id 11}]

          templates-count
          99]

      (with-redefs [domain.templates/get-templates-for-project
                    (fn [_ opts]
                      (and (= project (:project opts))
                           (= 2 (::middleware.pagination/page-number opts))
                           (= 3 (::middleware.pagination/page-size opts))
                           templates))

                    domain.templates/count-templates-for-project
                    (fn [project*] (when (= project project*) templates-count))]
        
        (is (= {:status 200
                ::middleware.pagination/items templates
                ::middleware.pagination/total-items-count templates-count}
               (sut/handle-get-project-templates request)))))))
