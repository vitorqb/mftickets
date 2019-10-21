(ns mftickets.inject
  (:require 
            [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections :as domain.templates.sections]))

(def inject
  {::domain.projects/count-templates
   domain.templates/count-templates-for-project

   ::domain.templates.inject/get-properties-for-templates
   domain.templates.properties/get-properties-for-templates

   ::domain.templates.inject/get-sections-for-templates
   domain.templates.sections/get-sections-for-templates})

