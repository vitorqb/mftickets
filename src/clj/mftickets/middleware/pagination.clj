(ns mftickets.middleware.pagination
  (:require [clojure.core.match :as match]
            [clojure.spec.alpha :as spec]))

;; Specs
(spec/def ::page-number number?)
(spec/def ::page-size number?)
(spec/def ::pagination-data (spec/keys :req [::page-number ::page-size]))
(spec/def ::items seqable?)
(spec/def ::total-items-count int?)
(spec/def ::response (spec/and (spec/keys :opt [::items ::total-items-count])
                               #(if (:items %) (:total-items-count %) true)))

;; Impl
(def default-page-number 1)
(def default-page-size 50)

(defn- assoc-page-number
  "Parses the page number from the request"
  [{{{page-number :pageNumber} :query} :parameters :as request}]
  (assoc request ::page-number (or page-number default-page-number)))

(defn- assoc-page-size
  "Parses the page size from the request"
  [{{{page-size :pageSize} :query} :parameters :as request}]
  (assoc request ::page-size (or page-size default-page-size)))

(defn- assoc-response-body-from-items
  "Given a ring response with ::items, assocs it's body"
  [{::keys [page-number page-size] :as request}
   {::keys [items total-items-count] :as response}]

  {:pre [(spec/assert ::response response) (spec/assert ::pagination-data request)]}

  (cond-> response
    items (assoc :body {:page-number page-number
                        :page-size page-size
                        :total-items-count total-items-count
                        :items items})))

;; API
(defn wrap-pagination-data
  "Middleware adding pagination data to the request."
  [handler]
  (fn i-wrap-pagination-data [request]
    (let [request* (-> request assoc-page-number assoc-page-size)
          response (handler request*)]
      (assoc-response-body-from-items request* response))))
