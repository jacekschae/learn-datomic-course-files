(ns cheffy.server
  (:require [cheffy.router :as router]
            [environ.core :refer [env]]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [datomic.client.api :as d]
            [datomic.dev-local :as dl]
            [clojure.edn :as edn]
            [cheffy.validation :as validation]))

(defn ident-has-attr?
  [db ident attr]
  (contains? (d/pull db {:eid ident :selector '[*]}) attr))

(comment
  (d/pull (d/db (:conn user/datomic)) {:eid :account/account-id :selector '[*]})
  (has-attr? (d/db (:conn user/datomic)) :account/account-id :db/ident)

  )

(defn load-dataset
  [conn]
  (let [db (d/db conn)
        tx #(d/transact conn {:tx-data %})]
    (when-not (ident-has-attr? db :account/account-id :db/ident)
      (tx (-> "src/resources/cheffy/schema.edn" slurp edn/read-string))
      (tx (-> "src/resources/cheffy/seed.edn" slurp edn/read-string)))
    (when-not (ident-has-attr? db :account/account-id :db.attr/preds)
      (tx validation/attr-pred))))

(defn app
  [env]
  (router/routes env))

(defmethod ig/prep-key :server/jetty
  [_ config]
  (merge config {:port (Integer/parseInt (env :port))}))

(defmethod ig/prep-key :auth/auth0
  [_ config]
  (merge config {:client-secret (env :auth0-client-secret)}))

(defmethod ig/init-key :server/jetty
  [_ {:keys [handler port] :as _config}]
  (println (str "\nServer running on port " port))
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/init-key :cheffy/app
  [_ config]
  (println "\nStarted app")
  (app config))

(defmethod ig/init-key :auth/auth0
  [_ auth0]
  (println "\nConfigured auth0")
  auth0)

(defmethod ig/init-key :db/datomic
  [_ config]
  (println "\nStarted DB")
  (let [db-name (select-keys config [:db-name])
        client (d/client (select-keys config [:server-type :system]))
        _ (d/create-database client db-name)
        conn (d/connect client db-name)]
    (load-dataset conn)
    (assoc config :conn conn)))

(defmethod ig/halt-key! :db/datomic
  [_ config]
  (println "\nStopping DB")
  (dl/release-db (select-keys config [:system :db-name])))

(defmethod ig/halt-key! :server/jetty
  [_ jetty]
  (.stop jetty))

(defn -main
  [config-file]
  (let [config (-> config-file slurp ig/read-string)]
    (-> config ig/prep ig/init)))