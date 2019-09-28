(ns mftickets.routes.services.projects-test
  (:require [mftickets.routes.services.projects :as sut]
            [clojure.test :as t :refer [is are deftest testing use-fixtures]]
            [mftickets.test-utils :as tu]
            [ring.mock.request :as mock.request]
            [mftickets.handler :refer [app]]
            [mftickets.middleware.context :as middleware.context]
            [mftickets.domain.projects :as domain.projects]))

(deftest test-handle-get
  (let [handle-get #'sut/handle-get]
    (is (= {:status 200 :body {:id 9}}
           (handle-get #::middleware.context{:project {:id 9}})))))

(deftest test-handle-get-projects
  (let [handle-get-projects #'sut/handle-get-projects 
        user {:id 999}
        projects [{:id 1} {:id 2}]
        request {:mftickets.auth/user user}]
    (with-redefs [domain.projects/get-projects-for-user #(if (= % user) projects)]
      (is (= {:status 200 :body projects} (handle-get-projects request))))))

(deftest test-integration

  (testing "Base integration test for get and get list"
    (tu/with-app
      (tu/with-db
        (tu/with-user-and-token [user token]

          (testing "Get single returns 404 if project does not exist"
            (let [response (-> (mock.request/request :get "/api/projects/121321")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 404 (:status response)))))

          ;; Adds a project
          (tu/gen-save! tu/project {:id 99})

          (testing "Get single returns 404 if user has no acces to project"
            (let [response (-> (mock.request/request :get "/api/projects/99")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 404 (:status response)))))

          (testing "Get many returns empty list if user has no acces to any project"
            (let [response (-> (mock.request/request :get "/api/projects")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 200 (:status response)))
              (is (= [] (-> response tu/decode-response-body)))))

          ;; Assign to the user
          (tu/gen-save! tu/users-projects {:user-id (:id user) :project-id 99})

          (testing "Get single returns 200 with some body if user has access to project"
            (let [response (-> (mock.request/request :get "/api/projects/99")
                               (tu/auth-header token)
                               ((app)))]
              (is (= 200 (:status response)))
              (is (not (nil? (:body response))))
              (is (= 99 (-> response tu/decode-response-body :id)))))

          (testing "Get many returns 200 with project user has access to"
            (let [response (-> (mock.request/request :get "/api/projects")
                               (tu/auth-header token)
                               ((app)))
                  body (-> response tu/decode-response-body)]
              (is (= 200 (:status response)))
              (is (= 1 (count body)))
              (is (= 99 (-> body first :id))))))))))
