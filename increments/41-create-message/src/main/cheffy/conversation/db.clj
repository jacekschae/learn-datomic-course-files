(ns cheffy.conversation.db
  (:require [datomic.client.api :as d]))

(def conversation-pattern
  [:conversation/conversation-id
   {:conversation/messages
    [:message/message-id
     :message/body
     {:message/owner
      [:account/account-id
       :account/display-name]}]}])

(defn find-unread-messages
  [{:keys [db]} {:keys [account-id conversation-id]}]
  (map first (d/q '[:find ?m
                    :in $ ?account-id ?conversation-id
                    :where
                    [?a :account/account-id ?account-id]
                    [?c :conversation/conversation-id ?conversation-id]
                    [?c :conversation/participants ?a]
                    [?c :conversation/messages ?m]
                    (not [?m :message/read-by ?a])]
                  db account-id conversation-id))

  )

(comment
  (let [db (d/db (:conn user/datomic))
        account-id "auth0|5fbf7db6271d5e0076903601"
        conversation-id #uuid"8d4ab926-d5cc-483d-9af0-19627ed468eb"]
    (d/q '[:find ?m
           :in $ ?account-id ?conversation-id
           :where
           [?a :account/account-id ?account-id]
           [?c :conversation/conversation-id ?conversation-id]
           [?c :conversation/participants ?a]
           [?c :conversation/messages ?m]
           (not [?m :message/read-by ?a])]
         db account-id conversation-id)))

(defn find-conversations-by-account-id
  [{:keys [db] :as env} {:keys [account-id]}]
  (let [conversations (mapv first (d/q '[:find (pull ?c pattern)
                                         :in $ ?account-id pattern
                                         :where
                                         [?a :account/account-id ?account-id]
                                         [?c :conversation/participants ?a]]
                                       db account-id conversation-pattern))]
    (mapv
      (fn [{:conversation/keys [conversation-id] :as conversation}]
        (assoc conversation :conversation/unread-messages (count (find-unread-messages
                                                                   env
                                                                   {:account-id account-id
                                                                    :conversation-id conversation-id}))))
      conversations)))

(defn transact-message
  [])

(defn transact-conversation
  [])

(defn find-messages-by-conversation-id
  [{:keys [db]} {:keys [conversation-id]}]
  (let [message-pattern [:message/message-id
                         :message/body
                         :message/created-at
                         {:message/owner
                          [:account/account-id
                           :account/display-name]}]]
    (->> (d/q '[:find (pull ?m pattern)
                :in $ ?conversation-id pattern
                :where
                [?e :conversation/conversation-id ?conversation-id]
                [?e :conversation/messages ?m]]
              db conversation-id message-pattern)
         (map first)
         (sort-by :message/created-at))))

(comment
  (find-messages-by-conversation-id
    {:db (d/db (:conn user/datomic))}
    {:conversation-id #uuid"8d4ab926-d5cc-483d-9af0-19627ed468eb"})

  (let [db (d/db (:conn user/datomic))
        conversation-id #uuid"8d4ab926-d5cc-483d-9af0-19627ed468eb"
        message-pattern [:message/message-id
                         :message/body
                         :message/created-at
                         {:message/owner
                          [:account/account-id
                           :account/display-name]}]]
    (->> (d/q '[:find (pull ?m pattern)
                :in $ ?conversation-id pattern
                :where
                [?e :conversation/conversation-id ?conversation-id]
                [?e :conversation/messages ?m]]
              db conversation-id message-pattern)
         (map first)
         (sort-by :message/created-at)))
  )

(defn read-messages
  [])
