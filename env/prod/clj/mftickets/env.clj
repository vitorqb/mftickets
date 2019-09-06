(ns mftickets.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[mftickets started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[mftickets has shut down successfully]=-"))
   :middleware identity})
