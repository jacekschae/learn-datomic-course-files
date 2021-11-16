(ns cheffy.account.handlers
  (:require [cheffy.auth0 :as auth0]
            [clj-http.client :as http]
            [muuntaja.core :as m]
            [ring.util.response :as rr]
            [datomic.client.api :as d]))

(defn create-account!
  [{:keys [env claims] :as _request}]
  (let [conn (get-in env [:datomic :conn])
        {:keys [sub name picture]} claims]
    (d/transact conn {:tx-data [{:account/account-id sub
                                 :account/display-name (or name "")
                                 :account/picture-url (or picture "")}]}))
  (rr/status 201))

(defn update-role-to-cook!
  [{:keys [env claims] :as _request}]
  (let [uid (:sub claims)
        client-secret (-> env :auth0)
        token (auth0/get-management-token client-secret)]
    (->> {:headers {"Authorization" (str "Bearer " token)}
          :cookie-policy :standard
          :content-type :json
          :throw-exceptions false
          :body (m/encode "application/json"
                  {:roles [(auth0/get-role-id token)]})}
      (http/post (str "https://learn-reitit-playground.eu.auth0.com/api/v2/users/" uid "/roles")))))


(defn delete-account!
  [{:keys [env claims] :as _request}]
  (let [conn (get-in env [:datomic :conn])
        account-id (:sub claims)
        client-secret (-> env :auth0)
        delete-auth0-account! (http/delete
                                (str "https://learn-reitit-playground.eu.auth0.com/api/v2/users/" account-id)
                                {:headers {"Authorization" (str "Bearer " (auth0/get-management-token client-secret))}})]
    (when (= (:status delete-auth0-account!) 204)
      (d/transact conn {:tx-data [[:db/retractEntity [:account/account-id account-id]]]})
      (rr/status 204))))