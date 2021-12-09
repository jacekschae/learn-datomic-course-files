(ns cheffy.conversation.routes
  (:require [cheffy.conversation.handlers :as conversation]
            [cheffy.middleware :as mw]))

(def routes
  ["/conversation" {:swagger {:tags ["conversations"]}
                    :middleware [[mw/wrap-db] [mw/wrap-auth0]]}
   [""
    {:get {:handler conversation/list-conversations
           :responses {200 {:body vector?}}
           :summary "List conversations"}
     :post {:handler conversation/create-message!
            :parameters {:body {:message-body string? :to string?}}
            :responses {201 {:body {:conversation-id string?}}}
            :summary "Start a conversation"}}]
   ["/:conversation-id" {:middleware [[mw/wrap-conversation-participant]]}
    [""
     {:get {:handler conversation/list-messages
            :parameters {:path {:conversation-id string?}}
            :responses {200 {:body any?}}
            :summary "List conversation messages"}
      :post {:handler conversation/create-message!
             :parameters {:path {:conversation-id string?}
                          :body {:message-body string? :to string?}}
             :responses {201 {:body {:conversation-id string?}}}
             :summary "Create message"}
      :put {:handler conversation/update-notifications!
            :parameters {:path {:conversation-id string?}}
            :responses {204 {:body nil?}}
            :summary "Update notifications"}}]]])