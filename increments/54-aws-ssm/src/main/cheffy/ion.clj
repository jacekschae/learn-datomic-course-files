(ns cheffy.ion
  (:require [integrant.core :as ig]
            [cheffy.components.auth0 :as auth0]
            [cheffy.components.datomic-cloud :as datomic-cloud]))

(def integrant-setup
  {:cheffy.server/app {:datomic (ig/ref ::datomic-cloud/db)
                       :auth0 (ig/ref ::auth0/auth)}
   ::auth0/auth {:client-secret "<CLIENT_SECRET>"}
   ::datomic-cloud/db {:server-type :ion
                       :region "ap-northeast-1"
                       :system "cheffy-prod"
                       :db-name "cheffy-prod"}})

(def handler
  (-> integrant-setup ig/prep ig/init :cheffy.server/app))