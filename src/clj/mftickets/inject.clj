(ns mftickets.inject
  (:require [mftickets.domain.projects :as domain.projects]
            [mftickets.domain.templates :as domain.templates]
            [mftickets.domain.templates.inject :as domain.templates.inject]
            [mftickets.domain.templates.properties :as domain.templates.properties]
            [mftickets.domain.templates.properties.create
             :as
             domain.templates.properties.create]
            [mftickets.domain.templates.properties.get
             :as
             domain.templates.properties.get]
            [mftickets.domain.templates.properties.update
             :as
             domain.templates.properties.update]
            [mftickets.domain.templates.sections :as domain.templates.sections]
            [mftickets.domain.templates.sections.create
             :as
             domain.templates.sections.create]
            [mftickets.domain.templates.sections.inject
             :as
             domain.templates.sections.inject]
            [mftickets.domain.templates.sections.update
             :as
             domain.templates.sections.update]
            [mftickets.domain.tickets.properties-values
             :as
             domain.tickets.properties-values]
            [mftickets.domain.tickets.properties-values.create
             :as
             domain.tickets.properties-values.create]
            [mftickets.domain.tickets.properties-values.create.inject
             :as
             domain.tickets.properties-values.create.inject]
            [mftickets.domain.tickets.properties-values.get
             :as
             domain.tickets.properties-values.get]
            [mftickets.domain.tickets.properties-values.get.inject
             :as
             domain.tickets.properties-values.get.inject]
            [mount.core :refer [defstate]]))

;; !!!! TODO -> Don't use injection for hierarchical imports, like
;; !!!!         templates -> templates.properties
(defstate inject
  :start 
  {::domain.projects/count-templates
   domain.templates/count-templates-for-project

   ::domain.templates.inject/get-properties-for-templates
   domain.templates.properties.get/get-properties-for-templates

   ::domain.templates.inject/get-sections-for-templates
   domain.templates.sections/get-sections-for-templates

   ::domain.templates.inject/update-section!
   domain.templates.sections.update/update-section!

   ::domain.templates.inject/create-section!
   domain.templates.sections.create/create-section!

   ::domain.templates.inject/delete-section!
   domain.templates.sections/delete-section!

   ::domain.templates.sections.inject/create-property!
   domain.templates.properties.create/create-property!

   ::domain.templates.sections.inject/update-property!
   domain.templates.properties.update/update-property!

   ::domain.templates.sections.inject/delete-property!
   domain.templates.properties/delete-property!

   ::domain.tickets.properties-values.get.inject/get-property
   domain.templates.properties.get/get-property

   ::domain.templates.sections.inject/get-properties-for-section
   domain.templates.properties.get/get-properties-for-section

   ::domain.tickets.properties-values.create.inject/get-property
   domain.templates.properties.get/get-property
   })
