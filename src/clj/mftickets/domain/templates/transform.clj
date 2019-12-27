(ns mftickets.domain.templates.transform
  (:require [com.rpl.specter :as s]))

(defn set-section-order
  [template section-id new-order]
  "Set's the templates section's `order` to `new-order`. Identifies the section by id."
  (s/setval
   [(s/must :sections) (s/filterer :id (s/pred= section-id)) s/FIRST :order]
   new-order
   template))

(defn get-section-order
  [template section-id]
  "Get's the templates' section's `order` usign section id."
  (s/select-first
   [:sections (s/filterer :id (s/pred= section-id)) s/FIRST :order]
   template))
