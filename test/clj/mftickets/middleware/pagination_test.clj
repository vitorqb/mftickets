(ns mftickets.middleware.pagination-test
  (:require [clojure.test :as t :refer [are deftest is testing use-fixtures]]
            [mftickets.middleware.pagination :as sut]
            [mftickets.test-utils :as tu]))

(use-fixtures :once tu/common-fixture)

;; Helpers
(defn- with-page-number
  [request page-number]
  (assoc-in request [:parameters :query :pageNumber] page-number))

(defn- with-page-size
  [request page-size]
  (assoc-in request [:parameters :query :pageSize] page-size))

;; Tests
(deftest test-wrap-pagination-data

  (testing "Assocs defaults if no args."
    (let [request {::foo ::bar}
          handler (sut/wrap-pagination-data identity)]
      (is (= (assoc request
                    ::sut/page-number sut/default-page-number
                    ::sut/page-size sut/default-page-size)
             (handler request)))))

  (testing "Given a request with page-number and page-size..."
    (let [page-number 2
          page-size 30
          request (-> {} (with-page-number page-number) (with-page-size page-size))
          handler (sut/wrap-pagination-data identity)
          response (handler request)]

      (testing "Assocs ::page-number"
        (is (= page-number (::sut/page-number response))))

      (testing "Assocs ::page-size"
        (is (= page-size (::sut/page-size response))))

      (testing "Given a response with ::items and ::total-items-count"
        (let [items [1 2 3]
              total-items-count 3
              response {::sut/items items ::sut/total-items-count total-items-count}
              handler (sut/wrap-pagination-data (constantly response))
              response* (handler request)]

          (testing "Assocs body"
            (is (= {:page-number page-number
                    :page-size page-size
                    :total-items-count total-items-count
                    :items items}
                   (:body response*)))))))))


(deftest test-assoc-page-number

  (let [assoc-page-number #'sut/assoc-page-number]

    (testing "No parameters -> assocs default page-number"
      (let [request {::foo ::bar}
            response (assoc-page-number request)]
        (is (= (assoc request ::sut/page-number sut/default-page-number)
               response))))

    (testing "With pageNumber -> assocs it"
      (let [page-number 22
            request (-> {} (with-page-number page-number))
            response (assoc-page-number request)]
        (is (= page-number (::sut/page-number response)))))

    (testing "With no pageNumber but pageSize, assocs default page-number"
      (let [page-size 2
            request (-> {} (with-page-size page-size))
            response (assoc-page-number request)]
        (is (= sut/default-page-number (::sut/page-number response)))))))

(deftest test-assoc-page-size

  (let [assoc-page-size #'sut/assoc-page-size]

    (testing "No parameters -> assocs page-size to default"
      (let [request {::foo ::bar}
            response (assoc-page-size request)]
        (is (= (assoc request ::sut/page-size sut/default-page-size)
               response))))

    (testing "With pageSize -> assoc page-size to it"
      (let [page-size 15
            request (-> {} (with-page-size page-size))
            response (assoc-page-size request)]
        (is (= page-size (::sut/page-size response)))))

    (testing "With no pageSize but pageNumber, assocs page-size to default."
      (let [page-number 2
            request (-> {} (with-page-number page-number))
            response (assoc-page-size request)]
        (is (= sut/default-page-size (::sut/page-size response)))))))

(deftest test-assoc-response-body-from-items

  (let [assoc-response-body-from-items #'sut/assoc-response-body-from-items]

    (testing "Skip if items is nil"
      (let [response {:body "Foo"}
            request {::sut/page-number 2 ::sut/page-size 20}]
        (are [x] (= response (assoc-response-body-from-items (merge request x) response))
          {}
          {::sut/items nil})))

    (testing "Assocs body if items is given."
      (let [page-number 3
            page-size 25
            items [:one :two]
            total-items-count 150
            request {::sut/page-number page-number
                     ::sut/page-size page-size}
            response {::sut/total-items-count total-items-count ::sut/items items}
            response* (assoc-response-body-from-items request response)]
        (is (= (assoc response
                      :body {:page-number page-number
                             :page-size page-size
                             :total-items-count total-items-count
                             :items items})
               response*))))))
