
(ns mftickets.routes.services.app-metadata-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.handler :refer [app]]
            [mftickets.routes.services.app-metadata :as sut]
            [mftickets.test-utils :as tu]
            [mftickets.utils.kw :as utils.kw]
            [ring.mock.request :as mock.request]))

(deftest test-assoc-template-property-types
  (let [types #{::foo}]
    (with-redefs [domain.templates.properties/get-property-types (constantly types)]
      (is (= {:template.properties.types types} (#'sut/assoc-template-property-types {} {}))))))

(deftest test-assoc-projects

  (let [request {:mftickets.auth/user {:id 1}}]

    (testing "When no project"
      (with-redefs [domain.projects/get-projects-for-user (constantly nil)]
        (is (= {:projects []} (#'sut/assoc-projects {} request)))))

    (testing "With projects"
      (let [project {:id 1}]
        (with-redefs [domain.projects/get-projects-for-user (constantly [project])]
          (is (= {:projects [project]} (#'sut/assoc-projects {} request))))))))

(deftest test-handle-get

  (testing "Integration"
    (tu/with-db
      (tu/with-app
        (tu/with-user-and-token [user token]

          (let [exp-template-property-types
                (into [] (map utils.kw/full-name (domain.templates.properties/get-property-types)))

                response-body
                {:template.properties.types exp-template-property-types
                 :projects []}]

            (testing "No projects"
              (is (= response-body
                     (-> (mock.request/request :get "/api/app-metadata")
                         (tu/auth-header token)
                         ((app))
                         (tu/decode-response-body)))))

            (let [project (tu/gen-save! tu/project)]
              (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id (:id project)})

              (testing "With projects"
                (is (= (assoc response-body :projects [project])
                       (-> (mock.request/request :get "/api/app-metadata")
                           (tu/auth-header token)
                           ((app))
                           (tu/decode-response-body))))))))))))
