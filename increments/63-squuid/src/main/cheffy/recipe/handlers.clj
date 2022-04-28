(ns cheffy.recipe.handlers
  (:require [cheffy.recipe.db :as recipe-db]
            [cheffy.responses :as responses]
            [ring.util.response :as rr]
            [com.yetanalytics.squuid :as sq]))

(defn list-all-recipes
  [{:keys [env claims] :as _request}]
  (let [account-id (:sub claims)]
    (rr/response (recipe-db/find-all-recipes (:datomic env) {:account-id account-id}))
    ))

(defn create-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [recipe-id (sq/generate-squuid)
        account-id (:sub claims)
        recipe (:body parameters)]
    (recipe-db/transact-recipe (:datomic env) (assoc recipe :recipe-id recipe-id :account-id account-id))
    (rr/created (str responses/base-url "/recipes/" recipe-id) {:recipe-id (str recipe-id)})
    ))

(defn retrieve-recipe
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        recipe (recipe-db/find-recipe-by-id (:datomic env) {:recipe-id (parse-uuid recipe-id)})]
    (if recipe
      (rr/response recipe)
      (rr/not-found {:type "recipe-not-found"
                     :message "Recipe not found"
                     :data (str "recipe-id " recipe-id)}))))

(defn update-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        account-id (:sub claims)
        recipe (:body parameters)]
    (recipe-db/transact-recipe (:datomic env) (assoc recipe :recipe-id (parse-uuid recipe-id) :account-id account-id))
    (rr/status 204)
    ))

(defn delete-recipe!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/retract-recipe (:datomic env) {:recipe-id (parse-uuid recipe-id)})
    (rr/status 204)
    ))

(defn create-step!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        step (:body parameters)
        step-id (str (sq/generate-squuid))]
    (recipe-db/transact-step (:datomic env) (assoc step :recipe-id recipe-id
                                                        :step-id step-id))
    (rr/created
      (str responses/base-url "/recipes/" recipe-id)
      {:step-id step-id})))

(defn update-step!
  [{:keys [env parameters] :as _request}]
  (let [step (:body parameters)
        recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/transact-step (:datomic env) (assoc step :recipe-id recipe-id))
    (rr/status 204)))

(defn delete-step!
  [{:keys [env parameters] :as _request}]
  (let [step (:body parameters)]
    (recipe-db/retract-step (:datomic env) step)
    (rr/status 204)))

(defn create-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        ingredient (:body parameters)
        ingredient-id (str (sq/generate-squuid))]
    (recipe-db/transact-ingredient (:datomic env) (assoc ingredient :recipe-id recipe-id
                                                                    :ingredient-id ingredient-id))
    (rr/created
      (str responses/base-url "/recipes/" recipe-id)
      {:ingredient-id ingredient-id})))

(defn update-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [ingredient (:body parameters)
        recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/transact-ingredient (:datomic env) (assoc ingredient :recipe-id recipe-id))
    (rr/status 204)))

(defn delete-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [ingredient (:body parameters)]
    (recipe-db/retract-ingredient (:datomic env) ingredient)
    (rr/status 204)))

(defn favorite-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [account-id (:sub claims)
        recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/favorite-recipe (:datomic env) {:recipe-id (parse-uuid recipe-id)
                                               :account-id account-id})
    (rr/status 204)))

(defn unfavorite-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [account-id (:sub claims)
        recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/unfavorite-recipe (:datomic env) {:recipe-id (parse-uuid recipe-id)
                                                 :account-id account-id})
    (rr/status 204)))