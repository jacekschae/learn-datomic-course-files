(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.core :as ig]
            [integrant.repl.state :as state]
            [datomic.client.api :as d]))

(ig-repl/set-prep!
  (fn []
    (let [config (-> "config/dev.edn" slurp ig/read-string)]
      (ig/load-namespaces config)
      config)))

(def start-dev ig-repl/go)
(def stop-dev ig-repl/halt)
(def restart-dev (do ig-repl/halt ig-repl/init))
(def reset-all ig-repl/reset-all)

(def app (-> state/system :cheffy.server/app))
(def datomic (-> state/system :cheffy.components.datomic-dev-local/db))

(comment

  (cheffy.auth0/get-management-token (-> state/system :cheffy.components.auth0/auth))
  (ig/load-namespaces
    (-> "config/dev.edn" slurp ig/read-string))

  (set! *print-namespace-maps* false)

  (d/q '[:find ?e ?v
         :where
         [?e :account/account-id ?v]]
    (d/db (:conn datomic)))


  (d/q '[:find ?e ?v ?display-name
         :in $ ?account-id
         :where
         [?e :recipe/recipe-id ?v]
         [?e :recipe/display-name ?display-name]
         [?e :recipe/owner ?account-id]]
    (d/db (:conn datomic)) [:account/account-id "mike@mailinator.com"])

  (d/pull (d/db (:conn datomic)) {:eid [:account/account-id "mike@mailinator.com"]
                                  :selector '[:account/account-id
                                              :account/display-name
                                              {:account/favorite-recipes
                                               [:recipe/display-name
                                                :recipe/recipe-id]}]})

  )