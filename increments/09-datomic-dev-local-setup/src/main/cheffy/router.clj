(ns cheffy.router
  (:require [clojure.string :as string]
            [cheffy.account.routes :as account]
            [cheffy.conversation.routes :as conversation]
            [cheffy.middleware :as mw]
            [cheffy.recipe.routes :as recipe]
            [muuntaja.core :as m]
            [reitit.coercion.spec :as coercion-spec]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.dev :as dev]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.spec :as rs]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]))

(def swagger-docs
  ["/swagger.json"
   {:get
    {:no-doc true
     :swagger {:basePath "/"
               :info {:title "Cheffy API Reference"
                      :description "The Cheffy API is organized around REST. Returns JSON, Transit (msgpack, json), or EDN  encoded responses."
                      :version "1.0.0"}}
     :handler (swagger/create-swagger-handler)}}])

(def router-config
  {:validate rs/validate
   ;:reitit.middleware/transform dev/print-request-diffs
   :exception pretty/exception
   :data {:coercion coercion-spec/coercion
          :muuntaja m/instance
          :middleware [swagger/swagger-feature
                       muuntaja/format-middleware
                       ;exception/exception-middleware
                       coercion/coerce-request-middleware
                       coercion/coerce-response-middleware]}})

(defn routes
  [env]
  (ring/ring-handler
    (ring/router
      [swagger-docs
       ["/v1"
        account/routes
        recipe/routes
        conversation/routes]]
      (router-config env))
    (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"}))))