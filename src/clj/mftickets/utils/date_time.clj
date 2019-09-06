(ns mftickets.utils.date-time
  (:require
   [java-time]))

(def str-format "yyyy-MM-dd'T'HH:mm:ss")

(defn now-as-str []
  "Returns now as a string"
  (java-time/with-clock (java-time/system-clock "UTC")
    (java-time/format str-format (java-time/local-date-time))))
