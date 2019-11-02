(ns mftickets.domain.templates.inject
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::get-properties-for-templates fn?)
(spec/def ::mk-sections-getter fn?)
