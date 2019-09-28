(ns mftickets.middleware.context
  "Middleware that adds context to a request."
  (:require
   [mftickets.domain.projects :as domain.projects]
   [mftickets.domain.users :as domain.users]))

(defn- user-has-access-to-project?
  "Does a user has access to a project?"
  [user project]
  {:pre [(not (nil? user)) (not (nil? project))]
   :post [#(boolean %)]}
  (contains? (domain.users/get-projects-ids-for-user user) (:id project)))

(defn wrap-user-has-access-to-project?
  "Middleware that returns 400 if a user does not have access to a project."

  ([handler] (wrap-user-has-access-to-project? handler {}))

  ([handler {:keys [no-access] :or {no-access {:status 404}}}]
   
   (fn [{::keys [project] :mftickets.auth/keys [user] :as request}]
     {:pre [(not (nil? project)) (not (nil? user))]}

     (if (user-has-access-to-project? user project)
       (handler request)
       no-access))))

(defn wrap-get-project
  "Wrapper that assocs a ::project to the request, if found.
  Uses `(get-in request :parameters :query :project-id)` as id.
  If does not found, returns `not-found`. Defaults to {:status 404}"

  ([handler] (wrap-get-project handler nil))

  ([handler {:keys [not-found request->project-id]
             :or {not-found {:status 404}
                  request->project-id #(-> % :parameters :query :project-id)}}]
   
   (fn [request]
     (if-let [project (-> request request->project-id domain.projects/get-project)]
       (-> request (assoc ::project project) handler)
       not-found))))

