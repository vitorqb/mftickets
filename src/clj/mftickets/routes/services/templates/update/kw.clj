(ns mftickets.routes.services.templates.update.kw
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::id (spec/nilable int?))
(spec/def ::project-id int?)
(spec/def ::name string?)
(spec/def ::creation-date string?)
(spec/def ::template-id int?)
(spec/def ::order (spec/nilable int?))
(spec/def ::template-section-id (spec/nilable int?))
(spec/def ::is-multiple boolean?)
(spec/def ::value-type keyword?)
(spec/def ::value string?)
(spec/def ::property-id (spec/nilable int?))

(spec/def ::options
  (spec/coll-of
   (spec/keys :req-un [::id ::property-id ::value])))

(spec/def ::properties
  (spec/coll-of
   (spec/keys :req-un [::name ::is-multiple ::value-type ::order ::id ::template-section-id]
              :opt-un [::options])))

(spec/def ::sections
  (spec/coll-of
   (spec/keys :req-un [::name ::order ::properties ::id ::template-id])))

(spec/def ::template
  (spec/keys :req-un [::project-id ::name ::sections ::id ::creation-date]))


