(ns cheffy.account.db
  (:require [datomic.client.api :as d])
  (:import (java.util UUID)))

(defn transact-account
  [{:keys [conn]} {:keys [sub name picture]}]
  (d/transact conn {:tx-data [{:account/account-id sub
                               :account/display-name (or name "")
                               :account/picture-url (or picture "")}]}))

(defn retract-account
  [{:keys [conn]} {:keys [account-id]}]
  (d/transact conn {:tx-data [[:db/retractEntity [:account/account-id account-id]]]}))

(comment

  (d/q '[:find ?e ?v
         :where [?e :account/account-id ?v]]
    (d/db (:conn user/datomic)))

  (transact-account
    user/datomic
    {:sub (str "auth0|" (UUID/randomUUID))
     :name "name"
     :picture "picture"})

  (retract-account
    user/datomic
    {:account-id "auth0|999429f7-79a3-480d-bdcf-efa1b6ea0650"})

  )

