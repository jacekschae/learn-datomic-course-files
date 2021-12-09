(ns cheffy.components.auth0
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::auth
  [_ config]
  (println "\nConfigured auth0")
  config)
