(ns cheffy.conversation.handlers
  (:require [ring.util.response :as rr]
            [cheffy.conversation.db :as conversation-db]
            [cheffy.responses :as responses]))

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
        {:conversation-id (parse-uuid conversation-id)}))))

(defn create-message!
  [{:keys [parameters env claims] :as _request}]
  (let [conversation-id (if-let [s (-> parameters :path :conversation-id)]
                          (parse-uuid s)
                          (random-uuid))
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
    (conversation-db/read-messages
      (:datomic env)
      {:account-id account-id
       :conversation-id conversation-id})
    (rr/status 204)
    ))