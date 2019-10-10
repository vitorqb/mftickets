(ns mftickets.inject
  (:require
   [mftickets.domain.projects :as domain.projects]
   [mftickets.domain.templates :as domain.templates]))

(def inject
  {::domain.projects/count-templates domain.templates/count-templates-for-project})

