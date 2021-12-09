(ns cheffy.conversation.handlers
  (:require [ring.util.response :as rr]
            [cheffy.conversation.db :as conversation-db]
            [cheffy.responses :as responses])
  (:import (java.util UUID)))

(defn list-conversations
  [{:keys [env claims] :as _request}]
  (let [account-id (:sub claims)]
    (rr/response
      (conversation-db/find-conversations-by-account-id
        (:datomic env)
        {:account-id account-id}))
    ))

(defn list-messages
  [{:keys [env parameters] :as _request}]
  (let [conversation-id (-> parameters :path :conversation-id)]
    (rr/response
      (conversation-db/find-messages-by-conversation-id
        (:datomic env)
        {:conversation-id (UUID/fromString conversation-id)}))))

(defn create-message!
  [{:keys [parameters env claims] :as _request}]
  (let [conversation-id (if-let [s (-> parameters :path :conversation-id)]
                          (UUID/fromString s)
                          (UUID/randomUUID))
        message (:body parameters)
        from (:sub claims)]
    (conversation-db/transact-message
      (:datomic env)
      (assoc message
        :conversation-id conversation-id
        :from from))
    (rr/created
      (str responses/base-url "/v1/conversations/" conversation-id)
      {:conversation-id (str conversation-id)})
    ))

(defn update-notifications!
  [{:keys [claims env parameters] :as _request}]
  (let [account-id (:sub claims)
        conversation-id (-> parameters :path :conversation-id)]
    ;; FIXME: conversation-db/read-messages
    ))