(ns cheffy.recipe.db
  (:require [datomic.client.api :as d])
  (:import (java.util UUID)))

(def recipe-pattern
  [:recipe/recipe-id
   :recipe/prep-time
   :recipe/display-name
   :recipe/image-url
   :recipe/public?
   :account/_favorite-recipes
   :recipe/favorite-count
   {:recipe/owner
    [:account/account-id
     :account/display-name]}
   {:recipe/steps
    [:step/step-id
     :step/description
     :step/sort-order]}
   {:recipe/ingredients
    [:ingredient/ingredient-id
     :ingredient/display-name
     :ingredient/amount
     :ingredient/measure
     :ingredient/sort-order]}])

(defn query-result->recipe
  [[{:account/keys [_favorite-recipes] :as recipe}]]
  (-> recipe
      (assoc :recipe/favorite-count (count _favorite-recipes))
      (dissoc :account/_favorite-recipes)))

(defn find-all-recipes
  [{:keys [db]} {:keys [account-id]}]
  (let [public (mapv query-result->recipe (d/q '[:find (pull ?e pattern)
                                 :in $ pattern
                                 :where [?e :recipe/public? true]]
                            db recipe-pattern))]
    (if account-id
      (let [drafts (mapv query-result->recipe (d/q '[:find (pull ?e pattern)
                                      :in $ ?account-id pattern
                                      :where
                                      [?owner :account/account-id ?account-id]
                                      [?e :recipe/owner ?owner]
                                      [?e :recipe/public? false]]
                                 db account-id recipe-pattern))]
        {:drafts drafts
         :public public})
      {:public public})))

(comment
  (find-all-recipes
    {:db (d/db (:conn user/datomic))}
    {:account-id "auth0|5fbf7db6271d5e0076903601"})

  ; public
  (mapv first (let [db (d/db (:conn user/datomic))]
               (d/q '[:find (pull ?e pattern)
                      :in $ pattern
                      :where [?e :recipe/public? true]]
                 db recipe-pattern)))

  ; drafts
  (mapv first (let [db (d/db (:conn user/datomic))
                    account-id "auth0|5fbf7db6271d5e0076903601"]
                (d/q '[:find (pull ?e pattern)
                       :in $ ?account-id pattern
                       :where
                       [?owner :account/account-id ?account-id]
                       [?e :recipe/owner ?owner]
                       [?e :recipe/public? false]]
                  db account-id recipe-pattern)))

  )

(defn transact-recipe
  [{:keys [conn]} {:keys [recipe-id account-id name public prep-time img]}]
  (d/transact conn {:tx-data [{:recipe/recipe-id recipe-id
                               :recipe/display-name name
                               :recipe/public? (or public false)
                               :recipe/prep-time prep-time
                               :recipe/image-url img
                               :recipe/owner [:account/account-id account-id]
                               :db/ensure :recipe/validate}]}))

(defn find-recipe-by-id
  [{:keys [db]} {:keys [recipe-id]}]
  (-> (d/q '[:find (pull ?e pattern)
             :in $ ?recipe-id pattern
             :where [?e :recipe/recipe-id ?recipe-id]]
           db recipe-id recipe-pattern)
      (first)
      (query-result->recipe)))

(comment

  (-> (let [db (d/db (:conn user/datomic))]
        (d/q '[:find (pull ?e pattern)
               :in $ ?recipe-id pattern
               :where [?e :recipe/recipe-id ?recipe-id]]
             db #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46" recipe-pattern))
      (first)
      (query-result->recipe))

  )

(defn retract-recipe
  [{:keys [conn]} {:keys [recipe-id]}]
  (d/transact conn {:tx-data [[:db/retractEntity [:recipe/recipe-id recipe-id]]]}))

(defn transact-step
  [{:keys [conn]} {:keys [recipe-id step-id description sort]}]
  (d/transact conn {:tx-data [{:recipe/recipe-id (UUID/fromString recipe-id)
                               :recipe/steps [{:step/step-id (UUID/fromString step-id)
                                               :step/description description
                                               :step/sort-order sort
                                               :db/ensure :step/validate}]}]}))

(defn retract-step
  [{:keys [conn]} {:keys [step-id]}]
  (d/transact conn {:tx-data [[:db/retractEntity [:step/step-id (UUID/fromString step-id)]]]}))

(defn transact-ingredient
  [{:keys [conn]} {:keys [recipe-id ingredient-id amount measure sort name]}]
  (d/transact conn {:tx-data [{:recipe/recipe-id (UUID/fromString recipe-id)
                               :recipe/ingredients [{:ingredient/ingredient-id (UUID/fromString ingredient-id)
                                                     :ingredient/amount amount
                                                     :ingredient/measure measure
                                                     :ingredient/display-name name
                                                     :ingredient/sort-order sort
                                                     :db/ensure :ingredient/validate}]}]}))

(defn retract-ingredient
  [{:keys [conn]} {:keys [ingredient-id]}]
  (d/transact conn {:tx-data [[:db/retractEntity [:ingredient/ingredient-id ingredient-id]]]}))



(defn favorite-recipe
  [{:keys [conn db]} {:keys [recipe-id account-id]}]
  (let [already-favorite (ffirst (d/q '[:find ?r
                                        :in $ ?recipe-id ?account-id
                                        :where
                                        [?a :account/account-id ?account-id]
                                        [?r :recipe/recipe-id ?recipe-id]
                                        [?a :account/favorite-recipes ?r]]
                                      db recipe-id account-id))]
    (when-not already-favorite
      (d/transact conn {:tx-data [{:account/account-id account-id
                                   :account/favorite-recipes [[:recipe/recipe-id recipe-id]]}]}))))



(defn unfavorite-recipe
  [{:keys [conn]} {:keys [recipe-id account-id]}]
  (d/transact conn {:tx-data [[:db/retract
                               [:account/account-id account-id]
                               :account/favorite-recipes [:recipe/recipe-id recipe-id]]]}))

(comment

  :inc
  ::inc

  'inc-recipe
  `inc-recipe

  (d/pull (d/db (:conn user/datomic)) '[*] [:recipe/recipe-id #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46"])

  (unfavorite-recipe
    (assoc user/datomic :db (d/db (:conn user/datomic)))
    {:recipe-id #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46"
     :account-id "auth0|5fbf7db6271d5e0076903601"}
    )

  (favorite-recipe
    (assoc user/datomic :db (d/db (:conn user/datomic)))
    {:recipe-id #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46"
     :account-id "auth0|5fbf7db6271d5e0076903601"}
    )

  (d/q '[:find (pull ?a [*])
         :in $ ?account-id
         :where [?a :account/account-id ?account-id]]
       (d/db (:conn user/datomic)) "auth0|5fbf7db6271d5e0076903601")

  (d/q '[:find (pull ?r [:recipe/recipe-id :recipe/favorite-count])
         :in $ ?recipe-id
         :where [?r :recipe/recipe-id ?recipe-id]]
       (d/db (:conn user/datomic)) #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46")

  )