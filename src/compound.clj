(ns compound
  (:require
   [clojure.core.match :refer [match]]
   [clojure.pprint :refer [pprint]]
   [compound.interest :as interest]
   [compound.style :as style]
   [hiccup.core :refer [html]]
   [jsonista.core :as json]
   [malli.core :as m]
   [malli.transform :as mt]
   [ring.middleware.defaults :as defaults]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(def Wallet
  "Map that satisfies A = P(1 + r/n)^nt."
  [:map
   [:m decimal?]
   [:p decimal?]
   [:r decimal?]])

(def default-wallet
  {:p (bigdec 1000)
   :m (bigdec 100)
   :r (bigdec 0.01)})

(defn parse-wallet
  [params]
  (m/decode Wallet (select-keys params #{:m :p :r}) (mt/string-transformer)))

(comment
  (parse-wallet {:m "100" :p "1000" :r "3.142"}))

(defn markup-wallet
  [wallet]
  (let [{:keys [m p r]} wallet
        months          (interest/monthly p wallet)]
    [:div#wallet
     [:table
      [:tr
       [:td "p"] [:td (interest/round-half-even p)]]
      [:tr
       [:td "m"] [:td (interest/round-half-even m)]]
      [:tr
       [:td "r"] [:td (interest/round-half-even r)]]
      [:tr
       [:td "A"] [:td (interest/round-half-even (:balance (last months)))]]]
     [:div
      [:div#chart]
      [:script
       (format "Compound.data = %s;"
               (json/write-value-as-string
                {:$schema  "https://vega.github.io/schema/vega-lite/v5.json"
                 :data     {:values months}
                 :mark     "bar"
                 :encoding {:y {:field "balance"
                                :type  "quantitative"
                                :axis  {:title "Balance"}}
                            :x {:field    "month"
                                :timeUnit "year-month"
                                :axis     {:title "Month"}}}}))]]]))

(defn layout
  [& content]
  (str "<!DOCTYPE html>"
       (html
        [:head
         [:meta {:charset "utf-8"}]
         [:title "Compound"]
         [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
         [:link {:rel "stylesheet" :href "/compound.css"}]
         [:script {:src "https://unpkg.com/htmx.org@1.5.0/dist/htmx.min.js"}]
         [:script {:src "https://cdn.jsdelivr.net/npm/vega@5.21.0"}]
         [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@5.2.0"}]
         [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@6.20.2"}]
         [:script "var Compound = {data: undefined};"]]
        [:body.subpixel-antialiased.bg-neutral-100
         [:div.bg-neutral-200.py-2.mb-8
          [:header {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"}
           [:h1.text-lg "Compound"]]]
         (into [:main {:class "flex-grow"}] content)
         [:div.py-2.mt-8
          [:footer {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"}
           "© 2022 Compound Labs"]]])))

(defn home-response
  [request]
  (let [{:keys [p m r]
         :as   wallet} default-wallet]
    {:status  200
     :headers {"content-type" "text/html; charset=utf-8"}
     :body
     (layout
      [:div {:class "max-w-7xl mx-auto p-4 sm:p-6 lg:p-8 bg-white overflow-hidden shadow rounded-lg"}
       [:form {:class      "space-y-8 divide-y divide-gray-200"
               :hx-post    "/calc"
               :hx-target  "#wallet"
               :hx-trigger "keyup change delay:500ms"}
        [:div
         [:div
          [:h3 {:class "text-lg leading-6 font-medium text-gray-900"} "Calculator"]
          [:p {:class "mt-1 max-w-2xl text-sm text-gray-500"} "Turns out all that geometry pays well."]]

         [:div {:class style/fieldset-classes}
          (anti-forgery-field)

          [:div {:class style/field-classes}
           [:label {:for "p" :class style/label-classes} "Initial balance"]
           [:div {:class style/input-container-classes}
            [:input {:type        "number"
                     :name        "p"
                     :id          "p"
                     :value       p
                     :placeholder "Initial balance"
                     :class       style/input-classes}]]]

          [:div {:class style/field-classes}
           [:label {:for "m" :class style/label-classes} "Monthly deposit"]
           [:div {:class style/input-container-classes}
            [:input {:type        "number"
                     :name        "m"
                     :id          "m"
                     :value       m
                     :placeholder "Monthly deposit"
                     :class       style/input-classes}]]]

          [:div {:class style/field-classes}
           [:label {:for "r" :class style/label-classes} "Interest rate"]
           [:div {:class style/input-container-classes}
            [:input {:type        "number"
                     :name        "r"
                     :id          "r"
                     :value       r
                     :step        "any"
                     :placeholder "Interest rate"
                     :class       style/input-classes}]]]]

         [:div.pt-5
          [:div.flex.justify-end
           (style/button {:type "submit"} "Calculate")]]]]

       (markup-wallet wallet)
       [:script (-> request ::config :bootstrap-script)]])}))

(defn calc-response
  [request]
  (let [wallet (-> request :params parse-wallet)]
    (pprint wallet)
    {:status  200
     :headers {"content-type" "text/html; charset=utf-8"}
     :body    (-> wallet markup-wallet html)}))

(defn not-found-response
  [_request]
  {:status 404
   :body   "Not found.\n"})

(defn make-router
  [config]
  (fn router
    [request]
    (let [request   (assoc request ::config config)
          responder (match [(:request-method request) (:uri request)]
                      [:get "/"] home-response
                      [:post "/calc"] calc-response
                      :else not-found-response)]
      (responder request))))

(defn make-handler
  [config]
  (defaults/wrap-defaults (make-router config) defaults/site-defaults))
