(ns cheffy.account.db
  (:require [datomic.client.api :as d])
  (:import (java.util UUID)))

(defn transact-account
  [{:keys [conn]} {:keys [sub name picture]}]
  (d/transact conn {:tx-data [{:account/account-id sub
                               :account/display-name (or name "")
                               :account/picture-url (or picture "")}]}))

(comment

  (d/q '[:find ?e ?v
         :where [?e :account/account-id ?v]]
    (d/db (:conn user/datomic)))

  (transact-account
    user/datomic
    {:sub (str "auth0|" (UUID/randomUUID))
     :name "name"
     :picture "picture"})

  )

(defn retract-account
  [])