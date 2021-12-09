(ns cheffy.transaction-fn
  (:require [datomic.client.api :as d]))

(defn inc-favorite-recipe
  [db entity]
  (let [m (d/pull db '[*] entity)]
    [[:db/add (:db/id m) :recipe/favorite-count (inc (or (:recipe/favorite-count m) 1))]]))

(defn dec-favorite-recipe
  [db entity]
  (let [m (d/pull db '[*] entity)]
    [[:db/add (:db/id m) :recipe/favorite-count (dec (:recipe/favorite-count m))]]))