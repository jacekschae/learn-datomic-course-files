(ns cheffy.test-system
  (:require [clojure.test :refer :all]
            [integrant.repl.state :as state]
            [ring.mock.request :as mock]
            [muuntaja.core :as m]
            [cheffy.auth0 :as auth0]
            [clj-http.client :as http]))

(defn get-test-token
  [email]
  (->> {:content-type :json
        :cookie-policy :standard
        :body (m/encode "application/json"
                {:client_id "ts5NfJYbsIZ6rvhmbKykF9TkWz0tKcGS"
                 :audience "https://learn-reitit-playground.eu.auth0.com/api/v2/"
                 :grant_type "password"
                 :username email
                 :password "s#m3R4nd0m-pass"
                 :scope "openid profile email"})}
    (http/post "https://learn-reitit-playground.eu.auth0.com/oauth/token")
    (m/decode-response-body)
    :access_token))

(defn create-auth0-test-user
  [{:keys [connection email password]}]
  (let [auth0 (-> state/system :cheffy.components.auth0/auth)]
    (->> {:headers {"Authorization" (str "Bearer " (auth0/get-management-token auth0))}
          :throw-exceptions false
          :content-type :json
          :cookie-policy :standard
          :body (m/encode "application/json"
                  {:connection connection
                   :email email
                   :password password})}
      (http/post "https://learn-reitit-playground.eu.auth0.com/api/v2/users")
      (m/decode-response-body))))

(def token (atom nil))

(defn test-endpoint
  ([method uri]
   (test-endpoint method uri nil))
  ([method uri opts]
   (let [app (-> state/system :cheffy.server/app)
         request (app (-> (mock/request method uri)
                        (cond->
                          (:auth opts) (mock/header :authorization (str "Bearer " (or @token (get-test-token "testing@cheffy.app"))))
                          (:body opts) (mock/json-body (:body opts)))))]
     (update request :body (partial m/decode "application/json")))))

(comment
  (get-test-token "testing@cheffy.app")

  (let [request (test-endpoint :get "/v1/recipes")
        decoded-request (m/decode-response-body request)]
    (assoc request :body decoded-request))
  (test-endpoint :get "/v1/recipes")

  )