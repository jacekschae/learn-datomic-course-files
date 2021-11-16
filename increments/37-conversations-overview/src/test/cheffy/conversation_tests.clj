(ns cheffy.conversation-tests
  (:require [clojure.test :refer :all]
            [cheffy.test-system :as ts]
            [integrant.repl.state :as state]))

(def account-id "auth0|5fbf7db6271d5e0076903601")

(def conversation-id (atom nil))

(defn conversation-fixture
  [f]
  (let [conn (-> state/system :db/datomic :conn)]
    (ts/create-auth0-test-user
      {:connection "Username-Password-Authentication"
       :email "account-tests@cheffy.app"
       :password "s#m3R4nd0m-pass"})
    (reset! ts/token (ts/get-test-token "account-tests@cheffy.app"))
    (ts/test-endpoint :post "/v1/account" {:auth true})
    (f)
    (ts/test-endpoint :delete "/v1/account" {:auth true})
    (reset! ts/token nil)))

(use-fixtures :once conversation-fixture)

(deftest conversation-tests

  (testing "Create message"
    (testing "without conversation"
      (let [{:keys [status body]} (ts/test-endpoint :post "/v1/conversation"
                                    {:auth true :body {:to           "mike@mailinator.com"
                                                       :message-body "Test Message"}})]
        (reset! conversation-id (:conversation_id body))
        (is (= 201 status))))

    (testing "with conversation"
      (let [{:keys [status body]} (ts/test-endpoint :post (str "/v1/conversation/" @conversation-id)
                                    {:auth true :body {:to           "mike@mailinator.com"
                                                       :message-body "Second Test Message"}})]
        (is (= 201 status)))))

  (testing "List user conversations"
    (let [{:keys [status body]} (ts/test-endpoint :get "/v1/conversation" {:auth true})]
      (is (= (-> body (first) :conversation_id) @conversation-id))
      (is (= 200 status))))

  (testing "List conversation messages"
    (let [{:keys [status body]} (ts/test-endpoint :get (str "/v1/conversation/" @conversation-id)
                                  {:auth true})]
      (is (= 200 status))
      (is (= 2 (count body)))
      (is (= (:messages/conversation_id (first body) @conversation-id)))))

  (testing "Clear notifications"
    (let [{:keys [status]} (ts/test-endpoint :put (str "/v1/conversation/" @conversation-id)
                             {:auth true})]
      (is (= 204 status)))))