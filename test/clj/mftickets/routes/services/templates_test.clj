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
   [ring.mock.request :as mock.request]))

(deftest test-assoc-sections

  (let [assoc-sections #'sut/assoc-sections]

    (testing "Base"
      (let [sections [{:id 1} {:id 2}]]
        (with-redefs [domain.templates.sections/get-sections-for-template (constantly sections)]
          (is (= {:sections sections}
                 (assoc-sections {}))))))))

(deftest test-get-template

  (testing "Non-existing template"
    (with-redefs [domain.templates/get-raw-template (constantly nil)]
      (is (= {:status 404}
             (sut/handle-get {:parameters {:path {:id 1}}})))))

  (testing "Existing template"
    (with-redefs [domain.templates/get-raw-template #(hash-map :id %)
                  domain.templates.sections/get-sections-for-template (constantly [{:id 9}])]
      (let [response (sut/handle-get {:parameters {:path {:id 9}}})]
        (are [ks f] (f (get-in response ks ::nf))
          [:status]   #(= 200 %)
          [:body :id] #(= 9 %)
          [:body :sections] #(= [{:id 9}] %))))))

(deftest test-handle-get

  (testing "Inexistant template returns 404"
    (with-redefs [])))

(deftest test-app-integration-get-template

  (testing "Existing template"
    (test-utils/with-app
      (test-utils/with-db
        (db.prefill/run-prefills! db.core/*db*)
        (let [req (-> (mock.request/request :get "/api/templates/1"))
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
              [:sections :properties] #(= (count %) 5))))))))
