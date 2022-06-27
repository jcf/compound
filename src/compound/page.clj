(ns compound.page
  (:require
   [clojure.spec.alpha :as s]
   [compound.domain]
   [compound.icon :as icon]
   [hiccup.page]
   [hiccup.util]
   [hiccup2.core :as hiccup]))

;;; ----------------------------------------------------------------------------
;;; Vector -> String

(defn html
  "Renders the given content inside an `html` tag."
  [content]
  (str
   (hiccup2.core/html {:mode :html}
                      (hiccup.page/doctype :html5)
                      content)))

(defmacro html-fragment
  [options & content]
  `(str (hiccup2.core/html ~options ~@content)))

;;; ----------------------------------------------------------------------------
;;; Unsafe

(def unsafely
  hiccup.util/raw-string)

;;; ----------------------------------------------------------------------------
;;; Panel

(defn panel
  [& content]
  [:div {:class "bg-white overflow-hidden shadow rounded-lg"}
   (into [:div {:class "px-4 py-5 sm:p-6"}] content)])

;;; ----------------------------------------------------------------------------
;;; Layout

(s/fdef layout
  :args (s/cat :page    :compound/page
               :content (s/* (s/or :str string? :vec vector?)))
  :ret  vector?)

(defn layout
  "Returns the Hiccup markup for the given `page` and its content."
  [page & content]
  (let [{:page/keys [body-attrs html-attrs title]} page]
    [:html html-attrs
     [:head
      [:meta {:charset "utf-8"}]
      [:title (if title
                (format "%s by Compound" (:page/title page))
                "Compound")]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:link {:rel "stylesheet" :href "/compound.css"}]

      ;; Scripts

      ;; TODO Move scripts into page.
      [:script {:src "https://unpkg.com/htmx.org@1.5.0/dist/htmx.min.js"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/vega@5.21.0"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@5.2.0"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@6.20.2"}]
      [:script "var Compound = {data: undefined};"]]

     [:body body-attrs
      [:div {:class "min-h-full flex flex-col"}

       ;; Navigation

       [:nav {:class "bg-white shadow-sm"}
        [:div {:class "max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"}
         [:div {:class "flex justify-between h-16"}
          [:div {:class "flex"}
           [:div {:class "flex-shrink-0 flex items-center"}
            [:h2 {:class "font-bold"} "Compound"]]
           [:div {:class "hidden sm:-my-px sm:ml-6 sm:flex sm:space-x-8"}
            [:a {:class        "border-indigo-500 text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                 :href         "/"
                 :aria-current "page"}
             "Home"]]]]]]

       ;; Header

       [:header
        [:div {:class "max-w-7xl mx-auto py-10 px-4 sm:px-6 lg:px-8"}
         [:h1 {:class "text-3xl font-bold leading-tight text-gray-900"}
          (or title "Compound")]]]

       ;; Main

       [:main {:class "flex-grow"}
        [:div {:class "max-w-7xl mx-auto sm:px-6 lg:px-8"}
         content]]

       ;; Footer

       [:footer {:class "bg-white mt-10"}
        [:div {:class "max-w-7xl mx-auto py-12 px-4 overflow-hidden sm:px-6 lg:px-8"}
         [:nav
          {:class      "-mx-5 -my-2 flex flex-wrap justify-center"
           :aria-label "Footer"}
          (for [{:keys [href text]} [{:href "/" :text "About"}
                                     {:href "/" :text "Blog"}
                                     {:href "/" :text "Jobs"}
                                     {:href "/" :text "Press"}
                                     {:href "/" :text "Accessibility"}
                                     {:href "/" :text "Partners"}]]
            [:div {:class "px-5 py-2"}
             [:a {:class "text-base text-gray-500 hover:text-gray-900"
                  :href  href}
              text]])]

         [:div {:class "mt-8 flex justify-center space-x-6"}
          (for [{:keys [href name path]} [{:href "/" :name "Facebook" :path icon/facebook}
                                          {:href "/" :name "Instagram" :path icon/instagram}
                                          {:href "/" :name "Twitter" :path icon/twitter}
                                          {:href "/" :name "GitHub" :path icon/github}
                                          {:href "/" :name "Dribbble" :path icon/dribbble}]]
            [:a {:href  href
                 :class "text-gray-400 hover:text-gray-500"}
             [:span {:class "sr-only"} name]
             [:svg {:class       "h-6 w-6"
                    :fill        "currentColor"
                    :viewbox     "0 0 24 24"
                    :aria-hidden "true"}
              [:path path]]])]

         [:p
          {:class "mt-8 text-center text-base text-gray-400"}
          "© 2020 Compound. All rights reserved."]]]]]]))
