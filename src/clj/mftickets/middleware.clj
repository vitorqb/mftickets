(ns mftickets.middleware
  (:require
    [mftickets.env :refer [defaults]]
    [mftickets.config :refer [env]]
    [mftickets.middleware.cors :as middleware.cors]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      middleware.cors/allow-cors-header
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))))
