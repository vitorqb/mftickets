(ns mftickets.middleware
  (:require
    [mftickets.env :refer [defaults]]
    [mftickets.config :refer [env]]
    [ring.middleware.session.cookie]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-base [handler]

  (let [{:keys [cookie-store-key]} env
        cookie-store (ring.middleware.session.cookie/cookie-store {:key cookie-store-key})]

    (-> ((:middleware defaults) handler)
        (wrap-defaults
         (-> site-defaults
             (assoc-in [:security :anti-forgery] false)
             (assoc-in [:session :store] cookie-store))))))
