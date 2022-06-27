(ns compound.next
  (:require
   [clojure.core.match :refer [match]]
   [clojure.pprint :refer [pprint]]
   [compound.interest :as interest]
   [compound.page :as page]
   [jsonista.core :as json]
   [malli.core :as m]
   [malli.transform :as mt]
   [ring.middleware.defaults :as defaults]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(def ^:private home-page
  {:page/html-attrs {:class ["h-full" "bg-neutral-100"]
                     :lang  "en-GB"}
   :page/body-attrs {:class ["h-full"]}
   :page/title      "Calculator"})

(def ^:private default-wallet
  {:m (bigdec 100)
   :p (bigdec 1000)
   :r (bigdec 0.01)})

(defn parse-wallet
  [params]
  (m/decode interest/Wallet (select-keys params #{:m :p :r}) (mt/string-transformer)))

(defn markup-form
  [wallet]
  (let [{:keys [m p r]} wallet
        hx-changed      {:hx-trigger "keyup changed delay:500ms"
                         :hx-post    "/calc"
                         :hx-swap    "outerHTML swap:0.2s settle:0.1s"
                         :hx-target  "#report"}]
    [:form {:class "space-y-8 divide-y divide-gray-200"}
     [:div {:class "grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6"}
      (page/unsafely (anti-forgery-field))
      (for [{:keys [id text value]} [{:id    "p"
                                      :value p
                                      :text  "Initial principle balance"}
                                     {:id    "m"
                                      :value m
                                      :text  "Monthly instalment"}
                                     {:id    "r"
                                      :value r
                                      :text  "Rate of interest"}]]
        [:div {:class "sm:col-span-2"}
         [:label {:for id :class "block text-sm font-medium text-gray-700"} text]
         [:div {:class "mt-1"}
          [:input (assoc hx-changed
                         :class "shadow-sm focus:ring-indigo-500 focus:border-indigo-500 block w-full sm:text-sm border-gray-300 rounded-md"
                         :id        id
                         :inputmode "numeric"
                         :name      id
                         :step      "any"
                         :value     value
                         :type      "text")]]])]]))

(defn markup-report
  [wallet]
  (let [{:keys [p]} wallet
        months      (interest/monthly p wallet)]
    [:div#report {:class "pt-8"}
     [:div#chart {:class "w-full" :style "height: 400px;"}]
     [:script
      (page/unsafely
       (format "Compound.data = %s;"
               (json/write-value-as-string
                {:$schema    "https://vega.github.io/schema/vega-lite/v5.json"
                 :data       {:values months}
                 :mark       "line"
                 :encoding   {:y {:field "balance"
                                  :type  "quantitative"
                                  :axis  {:title false}}
                              :x {:field    "month"
                                  :timeUnit "year-month"
                                  :axis     {:title  false
                                             :format "%Y"}}}
                 :background "transparent"
                 :height     "container"
                 :width      "container"})))]]))

(defn calc-response
  [request]
  (let [wallet (-> request :params parse-wallet)]
    (pprint wallet)
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (-> wallet markup-report page/html-fragment)}))

(defn not-found-response
  [_request]
  {:status 404
   :body   "Not found.\n"})

(defn home-response
  [request]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (page/html
    (page/layout
     home-page
     (page/panel (markup-form default-wallet))
     (markup-report default-wallet)
     [:script (-> request :compound/config :bootstrap-script page/unsafely)]))})

(defn router
  [request]
  (let [responder (match [(:request-method request) (:uri request)]
                    [:get "/"] home-response
                    [:post "/calc"] calc-response
                    :else not-found-response)]
    (responder request)))

(defn wrap-config
  [handler config]
  (fn [request]
    (handler (assoc request :compound/config config))))

(defn make-handler
  [config]
  (-> router
      (wrap-config config)
      (defaults/wrap-defaults (merge-with merge
                                          defaults/site-defaults
                                          (::site-defaults config)))))
