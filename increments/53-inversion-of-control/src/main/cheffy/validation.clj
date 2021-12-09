(ns cheffy.validation)

(defn account-id?
  [s]
  (<= 3 (count s) 120))

(def attr-pred
  [{:db/ident :account/account-id
    :db.attr/preds `account-id?}])

(def entity-attrs
  [{:db/ident :account/validate
    :db.entity/attrs [:account/account-id]}
   {:db/ident :recipe/validate
    :db.entity/attrs [:recipe/recipe-id :recipe/display-name :recipe/owner :recipe/prep-time]}
   {:db/ident :step/validate
    :db.entity/attrs [:step/step-id :step/description :step/sort-order]}
   {:db/ident :ingredient/validate
    :db.entity/attrs [:ingredient/ingredient-id :ingredient/display-name :ingredient/amount :ingredient/measure :ingredient/sort-order]}
   {:db/ident :conversation/validate
    :db.entity/attrs [:conversation/conversation-id :conversation/participants]}
   {:db/ident :message/validate
    :db.entity/attrs [:message/message-id :message/body :message/owner :message/created-at]}])

(def entity-preds
  [])