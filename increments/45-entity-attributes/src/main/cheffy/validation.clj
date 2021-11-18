(ns cheffy.validation)

(defn account-id?
  [s]
  (<= 3 (count s) 120))

(def attr-pred
  [{:db/ident :account/account-id
    :db.attr/preds `account-id?}])