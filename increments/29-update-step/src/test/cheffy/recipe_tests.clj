(ns cheffy.recipe-tests
  (:require [clojure.test :refer :all]
            [cheffy.server :refer :all]
            [cheffy.test-system :as ts]))

(defn recipe-fixture
  [f]
  (ts/create-auth0-test-user
    {:connection "Username-Password-Authentication"
     :email      "recipe-tests@cheffy.app"
     :password   "s#m3R4nd0m-pass"})
  (reset! ts/token (ts/get-test-token "recipe-tests@cheffy.app"))
  (ts/test-endpoint :post "/v1/account" {:auth true})
  (ts/test-endpoint :put "/v1/account" {:auth true})
  (reset! ts/token (ts/get-test-token "recipe-tests@cheffy.app"))
  (f)
  (ts/test-endpoint :delete "/v1/account" {:auth true})
  (reset! ts/token nil))

(use-fixtures :once recipe-fixture)

(def recipe-id (atom nil))

(def step-id (atom nil))

(def ingredient-id (atom nil))

(def recipe
  {:name      "Spinach salad"
   :prep-time 10
   :public    false
   :img       ""})

(def update-recipe
  (assoc recipe :public true))

(deftest recipes-tests
  (testing "List recipes"
    (testing "with auth -- public and drafts"
      (let [{:keys [status body]} (ts/test-endpoint :get "/v1/recipes" {:auth true})]
        (is (= 200 status))
        (is (vector? (:public body)))
        (is (vector? (:drafts body)))))

    (testing "without auth -- pubic"
      (let [{:keys [status body]} (ts/test-endpoint :get "/v1/recipes" {:auth false})]
        (is (= 200 status))
        (is (vector? (:public body)))
        (is (nil? (:drafts body)))))))

(deftest recipe-tests
  (testing "Create recipe"
    (let [{:keys [status body]} (ts/test-endpoint :post "/v1/recipes" {:auth true :body recipe})]
      (reset! recipe-id (:recipe-id body))
      (is (= status 201))))

  (testing "Retrieve recipe"
    (let [{:keys [status]} (ts/test-endpoint :get (str "/v1/recipes/" @recipe-id) {:auth true})]
      (is (= status 200))))

  (testing "Update recipe"
    (let [{:keys [status]} (ts/test-endpoint :put (str "/v1/recipes/" @recipe-id) {:auth true :body update-recipe})]
      (is (= status 204))))

  #_#_#_#_#_#_#_#_

  (testing "Favorite recipe"
    (let [{:keys [status]} (ts/test-endpoint :post (str "/v1/recipes/" @recipe-id "/favorite") {:auth true})]
      (is (= status 204))))

  (testing "Unfavorite recipe"
    (let [{:keys [status]} (ts/test-endpoint :delete (str "/v1/recipes/" @recipe-id "/favorite") {:auth true})]
      (is (= status 204))))

  (testing "Create step"
    (let [{:keys [status body]} (ts/test-endpoint :post (str "/v1/recipes/" @recipe-id "/steps")
                                  {:auth true :body {:description "My Test Step"
                                                     :sort        1}})]
      (reset! step-id (:step_id body))
      (is (= status 201))))

  (testing "Update step"
    (let [{:keys [status]} (ts/test-endpoint :put (str "/v1/recipes/" @recipe-id "/steps")
                             {:auth true :body {:step-id     @step-id
                                                :sort        2
                                                :description "Updated step"}})]
      (is (= status 204))))

  (testing "Delete step"
    (let [{:keys [status]} (ts/test-endpoint :delete (str "/v1/recipes/" @recipe-id "/steps")
                             {:auth true :body {:step-id @step-id}})]
      (is (= status 204))))

  (testing "Create ingredient"
    (let [{:keys [status body]} (ts/test-endpoint :post (str "/v1/recipes/" @recipe-id "/ingredients")
                                  {:auth true :body {:amount  2
                                                     :measure "30 grams"
                                                     :sort    1
                                                     :name    "My test ingredient"}})]
      (reset! ingredient-id (:ingredient_id body))
      (is (= status 201))))

  (testing "Update ingredient"
    (let [{:keys [status]} (ts/test-endpoint :put (str "/v1/recipes/" @recipe-id "/ingredients")
                             {:auth true :body {:ingredient-id @ingredient-id
                                                :name          "My updated name"
                                                :amount        5
                                                :measure       "50 grams"
                                                :sort          3}})]
      (is (= status 204))))

  (testing "Delete ingredient"
    (let [{:keys [status]} (ts/test-endpoint :delete (str "/v1/recipes/" @recipe-id "/ingredients")
                             {:auth true :body {:ingredient-id @ingredient-id}})]
      (is (= status 204))))

  (testing "Delete recipe"
    (let [{:keys [status]} (ts/test-endpoint :delete (str "/v1/recipes/" @recipe-id) {:auth true})]
      (is (= status 204)))))

(comment

  (ts/test-endpoint :get "/v1/recipes")
  (ts/test-endpoint :post "/v1/recipes/2ebf903e-56a6-44d0-96da-aaabdaa56686/favorite" {:auth true})
  (ts/test-endpoint :delete "/v1/recipes/be49e960-f5da-4a2e-8375-448901401ce7" {:auth true}))