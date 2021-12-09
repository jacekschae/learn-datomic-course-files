(ns cheffy.components.datomic-cloud
  (:require [datomic.client.api :as d]
            [cheffy.components.datomic :as datomic]
            [integrant.core :as ig]))

(defmethod ig/init-key ::db
  [_ config]
  (println "\nStarted DB")
  (let [db-name (select-keys config [:db-name])
        client (d/client (select-keys config [:server-type :system :region :endpoint]))
        list-databases (d/list-databases client {})]
    (when-not (some #{(:db-name config)} list-databases)    ;; is this required?
      (d/create-database client db-name))                   ;; or will calling create-db with the same name just return true?
    (let [conn (d/connect client db-name)]
      (datomic/load-dataset conn)
      (assoc config :conn conn))))