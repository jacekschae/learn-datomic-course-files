(ns cheffy.components.datomic
  (:require [datomic.client.api :as d]
            [cheffy.validation :as validation]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn ident-has-attr?
  [db ident attr]
  (contains? (d/pull db {:eid ident :selector '[*]}) attr))

(defn load-dataset
  [conn]
  (let [db (d/db conn)
        tx #(d/transact conn {:tx-data %})]
    (when-not (ident-has-attr? db :account/account-id :db/ident)
      (tx (-> (io/resource "cheffy/schema.edn") slurp edn/read-string))
      (tx (-> (io/resource "cheffy/seed.edn") slurp edn/read-string)))
    (when-not (ident-has-attr? db :account/account-id :db.attr/preds)
      (tx validation/attr-pred))
    (when-not (ident-has-attr? db :account/validate :db.entity/attrs)
      (tx validation/entity-attrs))))