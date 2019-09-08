(ns mftickets.utils.transform)

(defn remapkey
  "Remaps the key of a map into another key."
  [m k1 k2]
  (if (= (get m k1 ::na) ::na)
    m
    (-> m
        (assoc k2 (get m k1))
        (dissoc k1))))
