(ns cheffy.account.routes
  (:require [cheffy.account.handlers :as account]
            [cheffy.middleware :as mw]))

(def routes
  ["/account" {:swagger {:tags ["account"]}
               :middleware [[mw/wrap-auth0]]}
   [""
    {:post {:handler account/create-account!
            :responses {201 {:body nil?}}
            :summary "Create account"}
     :put {:handler account/update-role-to-cook!
           :responses {204 {:body nil?}}
           :summary "Update user role to cook"}
     :delete {:handler account/delete-account!
              :responses {204 {:body nil?}}
              :summary "Delete account"}}]])