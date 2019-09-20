(ns mftickets.utils.kw)

(defn full-name
  "Returns the full name of a keyword, including the namespace."
  [kw]
  (if-let [ns (namespace kw)]
    (str (namespace kw) "/" (name kw))
    (name kw)))
