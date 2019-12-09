(ns mftickets.inject
  (:require [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.domain.templates.sections :as domain.templates.sections]))

(def inject
  {::domain.projects/count-templates
   domain.templates/count-templates-for-project

   ::domain.templates.inject/get-properties-for-templates
   domain.templates.properties/get-properties-for-templates

   ::domain.templates.inject/get-sections-for-templates
   domain.templates.sections/get-sections-for-templates

   ::domain.templates.inject/update-section!
   domain.templates.sections/update-section!

   ::domain.templates.inject/create-section!
   domain.templates.sections/create-section!

   ::domain.templates.inject/delete-section!
   domain.templates.sections/delete-section!

   ::domain.templates.sections.inject/create-property!
   domain.templates.properties/create-property!

   ::domain.templates.sections.inject/update-property!
   domain.templates.properties/update-property!

   ::domain.templates.sections.inject/delete-property!
   domain.templates.properties/delete-property!

   ::domain.templates.sections.inject/get-properties-for-section
   domain.templates.properties/get-properties-for-section

   })
