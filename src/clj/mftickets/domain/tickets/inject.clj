(ns mftickets.domain.tickets.inject
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::create-property-value! ifn?)
(spec/def ::get-properties-for-ticket ifn?)
