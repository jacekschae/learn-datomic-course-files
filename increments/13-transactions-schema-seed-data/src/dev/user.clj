(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.core :as ig]
            [integrant.repl.state :as state]
            [cheffy.server]
            [datomic.client.api :as d]
            [clojure.edn :as edn]))

(ig-repl/set-prep!
  (fn [] (-> "src/dev/resources/config.edn" slurp ig/read-string)))

(def start-dev ig-repl/go)
(def stop-dev ig-repl/halt)
(def restart-dev ig-repl/reset)
(def reset-all ig-repl/reset-all)

(def app (-> state/system :cheffy/app))
(def datomic (-> state/system :db/datomic))



(comment
  (d/transact (:conn datomic) {:tx-data (-> "src/resources/schema.edn" slurp edn/read-string)})
  (d/transact (:conn datomic) {:tx-data (-> "src/resources/seed.edn" slurp edn/read-string)})

  )