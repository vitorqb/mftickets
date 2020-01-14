(ns mftickets.routes.services.templates.create.kw
  (:require [clojure.spec.alpha :as spec]))

(defn radio-properties-have-options [property]
  (or (not= (:value-type property) :templates.properties.types/radio)
      (contains? property :options)))

(spec/def ::id nil?)
(spec/def ::project-id int?)
(spec/def ::name string?)
(spec/def ::creation-date nil?)
(spec/def ::template-id nil?)
(spec/def ::order (spec/nilable int?))
(spec/def ::template-section-id nil?)
(spec/def ::is-multiple boolean?)
(spec/def ::value-type keyword?)
(spec/def ::value string?)
(spec/def ::property-id nil?)

(spec/def ::options
  (spec/coll-of
   (spec/keys :req-un [::value] :opt-un [::id ::property-id])))

(spec/def ::properties
  (spec/coll-of
   (spec/and
    (spec/keys :req-un [::name ::is-multiple ::value-type ::order]
               :opt-un [::id ::template-section-id ::options])
    radio-properties-have-options)))

(spec/def ::sections
  (spec/coll-of
   (spec/keys :req-un [::name ::order ::properties] :opt-un [::id ::template-id])))

(spec/def ::template
  (spec/keys :req-un [::project-id ::name ::sections] :opt-un [::id ::creation-date]))
