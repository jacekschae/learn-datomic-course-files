(ns cheffy.middleware
  (:require [ring.middleware.jwt :as jwt]
            [ring.util.response :as rr]))

(def wrap-auth0
  {:name ::auth0
   :description "Middleware for auth0 authentication and authorization"
   :wrap (fn [handler]
           (jwt/wrap-jwt
             handler
             {:issuers
              {"https://learn-reitit-playground.eu.auth0.com/"
               {:alg :RS256
                :jwk-endpoint "https://learn-reitit-playground.eu.auth0.com/.well-known/jwks.json"}}}))})

(def wrap-recipe-owner
  {:name ::recipe-owner
   :description "Middleware to check if a requestor is a recipe owner"
   :wrap (fn [handler]
           (fn [{:keys [claims env] :as request}]
             (let [account-id (:sub claims)
                   recipe-id (-> request :parameters :path :recipe-id)
                   recipe nil] ;; FIXME: recipe-db/find-recipe-by-id
               (if (= (-> recipe :recipe/owner :account/account-id) account-id)
                 (handler request)
                 (-> (rr/response {:message "You need to be the recipe owner"
                                   :data (str "recipe-id " recipe-id)
                                   :type :authorization-required})
                   (rr/status 401))))))})

(def wrap-manage-recipes
  {:name ::manage-recipes
   :description "Middleware to check if a user can manage recipes"
   :wrap (fn [handler]
           (fn [request]
             (let [roles (get-in request [:claims "https://api.learnreitit.com/roles"])]
               (if (some #{"manage-recipes"} roles)
                 (handler request)
                 (-> (rr/response {:message "You need to be a cook to manage recipes"
                                   :data (:uri request)
                                   :type :authorization-required})
                   (rr/status 401))))))})

(def wrap-conversation-participant
  {:name ::conversation-participant?
   :description "Middleware to check if a requester is an conversation participant"
   :wrap (fn [handler]
           (fn [{:keys [env claims] :as request}]
             (let [account-id (:sub claims)
                   conversation-id (-> request :parameters :path :conversation-id)
                   conversation nil] ;; FIXME: conversation
               (if conversation
                 (handler request)
                 (-> (rr/response {:message "You need to be a participant of the conversation to perform this action"
                                   :data (str "conversation-id " conversation-id)
                                   :type :authorization-required})
                   (rr/status 401))))))})