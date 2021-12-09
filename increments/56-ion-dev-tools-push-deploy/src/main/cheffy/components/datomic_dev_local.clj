(ns cheffy.components.datomic-dev-local
  (:require [cheffy.components.datomic :as datomic]
            [datomic.client.api :as d]
            [datomic.dev-local :as dl]
            [integrant.core :as ig]))

(defmethod ig/init-key ::db
  [_ config]
  (println "\nStarted DB")
  (let [db-name (select-keys config [:db-name])
        client (d/client (select-keys config [:server-type :system]))
        _ (d/create-database client db-name)
        conn (d/connect client db-name)]
    (datomic/load-dataset conn)
    (assoc config :conn conn)))

(defmethod ig/halt-key! ::db
  [_ config]
  (println "\nStopping DB")
  (dl/release-db (select-keys config [:system :db-name])))