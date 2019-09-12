(ns mftickets.middleware.cors)

(defn allow-cors-header [handler]
  (fn [request]
    (if (-> request :request-method (= :options))
      {:status 200
       :headers {"Access-Control-Allow-Origin" "*"
                 "Access-Control-Allow-Headers" "*"}}
      (-> request
          handler
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "*")))))
