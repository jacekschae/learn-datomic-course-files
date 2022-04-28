(ns cheffy.conversation.db
  (:require [datomic.client.api :as d])
  (:import (java.util Date)))

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

(defn find-conversations-by-account-id
  [{:keys [db] :as datomic} {:keys [account-id]}]
  (let [conversations (mapv first (d/q '[:find (pull ?c pattern)
                                         :in $ ?account-id pattern
                                         :where
                                         [?a :account/account-id ?account-id]
                                         [?c :conversation/participants ?a]]
                                       db account-id conversation-pattern))]
    (mapv
      (fn [{:conversation/keys [conversation-id] :as conversation}]
        (assoc conversation :conversation/unread-messages (count (find-unread-messages
                                                                   datomic
                                                                   {:account-id account-id
                                                                    :conversation-id conversation-id}))))
      conversations)))

(defn transact-message
  [{:keys [conn]} {:keys [conversation-id to from message-body]}]
  (let [message-id (random-uuid)]
    (d/transact conn {:tx-data [{:conversation/conversation-id conversation-id
                                 :conversation/participants (mapv #(vector :account/account-id %) [to from])
                                 :conversation/messages (str message-id)
                                 :db/ensure :conversation/validate}
                                {:db/id (str message-id)
                                 :message/message-id message-id
                                 :message/owner [:account/account-id from]
                                 :message/read-by [[:account/account-id from]]
                                 :message/body message-body
                                 :message/created-at (Date.)
                                 :db/ensure :message/validate}]})))

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

(defn read-messages
  [{:keys [db conn]} {:keys [account-id conversation-id ]}]
  (let [unread-messages (find-unread-messages
                          {:db db}
                          {:account-id account-id
                           :conversation-id conversation-id})
        tx-data
        (for [message unread-messages]
          [:db/add message :message/read-by [:account/account-id account-id]])]
    (when (seq unread-messages)
      (d/transact conn {:tx-data tx-data}))))

(comment

  ; transact-message
  (let [conn (:conn user/datomic)
        conversation-id (random-uuid)
        message-id (random-uuid)
        from "jade@mailinator.com"
        to "mark@mailinator.com"
        message-body (str "message-" (random-uuid))]
    (d/transact conn {:tx-data [{:conversation/conversation-id conversation-id
                                 :conversation/participants (mapv #(vector :account/account-id %) [to from])
                                 :conversation/messages (str message-id)}
                                {:db/id (str message-id)
                                 :message/message-id message-id
                                 :message/owner [:account/account-id from]
                                 :message/read-by [[:account/account-id from]]
                                 :message/body message-body
                                 :message/created-at (Date.)}]}))

  ; find conversation by account-id
  (d/q '[:find (pull ?c [*])
         :in $ ?account-id
         :where
         [?a :account/account-id ?account-id]
         [?c :conversation/participants ?a]]
       (d/db (:conn user/datomic)) "mark@mailinator.com")

  ; create tx-data and clear notifications
  (let [conn (:conn user/datomic)
        db (d/db conn)
        account-id "auth0|5fbf7db6271d5e0076903601"
        conversation-id #uuid"8d4ab926-d5cc-483d-9af0-19627ed468eb"
        unread-messages (find-unread-messages
                          {:db db}
                          {:account-id account-id
                           :conversation-id conversation-id})
        tx-data
        (for [message unread-messages]
          [:db/add message :message/read-by [:account/account-id account-id]])]
    (when (seq unread-messages)
      (d/transact conn {:tx-data tx-data}))
    )
  )
