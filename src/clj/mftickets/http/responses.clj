(ns mftickets.http.responses
  "Namespace with common http responses for mftickets.")

(defn validation-error
  "Returns an error response given a validation failure."
  [[error-key error-message]]
  {:status 400
   :body {:error-message error-message
          :error-key error-key}})

