(ns cheffy.recipe.handlers
  (:require [cheffy.recipe.db :as recipe-db]
            [cheffy.responses :as responses]
            [ring.util.response :as rr])
  (:import (java.util UUID)))

(defn list-all-recipes
  [{:keys [env claims] :as _request}]
  (let [account-id (:sub claims)]
    (rr/response (recipe-db/find-all-recipes (:datomic env) {:account-id account-id}))
    ))

(defn create-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [recipe-id (UUID/randomUUID)
        account-id (:sub claims)
        recipe (:body parameters)]
    (recipe-db/transact-recipe (:datomic env) (assoc recipe :recipe-id recipe-id :account-id account-id))
    (rr/created (str responses/base-url "/recipes/" recipe-id) {:recipe-id (str recipe-id)})
    ))

(defn retrieve-recipe
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        recipe (recipe-db/find-recipe-by-id (:datomic env) {:recipe-id (UUID/fromString recipe-id)})]
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
    (recipe-db/transact-recipe (:datomic env) (assoc recipe :recipe-id (UUID/fromString recipe-id) :account-id account-id))
    (rr/status 204)
    ))

(defn delete-recipe!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)]
    (recipe-db/retract-recipe (:datomic env) {:recipe-id (UUID/fromString recipe-id)})
    (rr/status 204)
    ))

(defn create-step!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        step (:body parameters)
        step-id (str (UUID/randomUUID))]
    ;; FIXME: recipe-db/transact-step
    ))

(defn update-step!
  [{:keys [env parameters] :as _request}]
  (let [step (:body parameters)
        recipe-id (-> parameters :path :recipe-id)]
    ;; FIXME: recipe-db/transact-step
    ))

(defn delete-step!
  [{:keys [env parameters] :as _request}]
  (let [step (:body parameters)]
    ;; FIXME: recipe-db/retract-step
    ))

(defn create-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [recipe-id (-> parameters :path :recipe-id)
        ingredient (:body parameters)
        ingredient-id (str (UUID/randomUUID))]
    ;; FIXME: recipe-db/transact-ingredient
    ))

(defn update-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [ingredient (:body parameters)
        recipe-id (-> parameters :path :recipe-id)]
    ;; FIXME: recipe-db/transact-ingredient
    ))

(defn delete-ingredient!
  [{:keys [env parameters] :as _request}]
  (let [ingredient (:body parameters)]
    ;; FIXME: recipe-db/retract-ingredient
    ))

(defn favorite-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [account-id (:sub claims)
        recipe-id (-> parameters :path :recipe-id)]
    ;; FIXME: recipe-db/favorite-recipe
    ))

(defn unfavorite-recipe!
  [{:keys [env claims parameters] :as _request}]
  (let [account-id (:sub claims)
        recipe-id (-> parameters :path :recipe-id)]
    ;; FIXME: recipe-db/unfavorite-recipe
    ))