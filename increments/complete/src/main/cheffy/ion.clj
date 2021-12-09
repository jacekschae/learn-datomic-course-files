(ns cheffy.ion
  (:require [integrant.core :as ig]
            [cheffy.components.auth0 :as auth0]
            [cheffy.components.datomic-cloud :as datomic-cloud]
            [datomic.ion :as ion]
            [cheffy.server :as server]))

(def integrant-setup
  {::server/app {:datomic (ig/ref ::datomic-cloud/db)
                 :auth0 (ig/ref ::auth0/auth)}
   ::auth0/auth {:client-secret (get (ion/get-params {:path "/datomic-shared/prod/cheffy/"}) "auth0-client-secret")}
   ::datomic-cloud/db {:server-type :ion
                       :region "ap-northeast-1"
                       :system "cheffy-prod"
                       :db-name "cheffy-prod"
                       :endpoint "https://08zozyjwc5.execute-api.ap-northeast-1.amazonaws.com"}})

(def app
  (delay
    (-> integrant-setup ig/prep ig/init ::server/app)))

(defn handler
  [req]
  (@app req))