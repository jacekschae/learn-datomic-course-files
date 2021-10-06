(ns cheffy.recipe.db
  (:require [datomic.client.api :as d]))

(def recipe-pattern
  [:recipe/recipe-id
   :recipe/prep-time
   :recipe/display-name
   :recipe/image-url
   :recipe/public?
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

(defn find-all-recipes
  [{:keys [db]} {:keys [account-id]}]
  (let [public (mapv first (d/q '[:find (pull ?e pattern)
                                 :in $ pattern
                                 :where [?e :recipe/public? true]]
                            db recipe-pattern))]
    (if account-id
      (let [drafts (mapv first (d/q '[:find (pull ?e pattern)
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
                               :recipe/owner [:account/account-id account-id]}]}))

(defn find-recipe-by-id
  [{:keys [db]} {:keys [recipe-id]}]
  (ffirst (d/q '[:find (pull ?e pattern)
                 :in $ ?recipe-id pattern
                 :where [?e :recipe/recipe-id ?recipe-id]]
               db recipe-id recipe-pattern)))

(comment

  (ffirst (let [db (d/db (:conn user/datomic))]
                   (d/q '[:find (pull ?e pattern)
                          :in $ ?recipe-id pattern
                          :where [?e :recipe/recipe-id ?recipe-id]]
                        db #uuid"a1995316-80ea-4a98-939d-7c6295e4bb46" recipe-pattern)))

  )

(defn retract-recipe
  [{:keys [conn]} {:keys [recipe-id]}]
  (d/transact conn {:tx-data [[:db/retractEntity [:recipe/recipe-id recipe-id]]]}))

(defn transact-step
  [])

(defn retract-step
  [])

(defn transact-ingredient
  [])

(defn retract-ingredient
  [])

(defn favorite-recipe
  [])

(defn unfavorite-recipe
  [])