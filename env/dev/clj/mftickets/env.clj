(ns mftickets.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [mftickets.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[mftickets started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[mftickets has shut down successfully]=-"))
   :middleware wrap-dev})
