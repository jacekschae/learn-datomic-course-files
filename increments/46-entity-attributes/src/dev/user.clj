(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.core :as ig]
            [integrant.repl.state :as state]
            [cheffy.server]
            [datomic.client.api :as d]
            [clojure.edn :as edn]))

(ig-repl/set-prep!
  (fn [] (-> "src/dev/resources/cheffy/config.edn" slurp ig/read-string)))

(def start-dev ig-repl/go)
(def stop-dev ig-repl/halt)
(def restart-dev ig-repl/reset)
(def reset-all ig-repl/reset-all)

(def app (-> state/system :cheffy/app))
(def datomic (-> state/system :db/datomic))

(comment

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