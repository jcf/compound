(ns compound.style
  (:require
   [clojure.string :as str]))

(defn combine
  [s xs]
  (str/join " " (into (if (nil? s) [] [s]) xs)))

(defn styled
  [tag-name attrs classes children]
  (into [tag-name (update attrs :class combine classes)] children))

(def fieldset-classes
  "mt-6 sm:mt-5 space-y-6 sm:space-y-5")

(def field-classes
  "sm:grid sm:grid-cols-3 sm:gap-4 sm:items-center sm:border-t sm:border-gray-200 sm:pt-5")

(def label-classes
  "block text-sm font-medium text-gray-700 sm:mt-px sm:pt-2")

(def input-container-classes
  "mt-1 sm:mt-0 sm:col-span-2")

(def input-classes
  "max-w-lg block w-full shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:max-w-xs sm:text-sm border-gray-300 rounded-md")

(defn button
  [attrs & children]
  (styled :button attrs ["inline-flex"
                         "items-center"
                         "px-6"
                         "py-3"
                         "border"
                         "border-transparent"
                         "text-base"
                         "font-medium"
                         "rounded-md"
                         "shadow-sm"
                         "text-white"
                         "bg-indigo-600"
                         "hover:bg-indigo-700"
                         "focus:outline-none"
                         "focus:ring-2"
                         "focus:ring-offset-2"
                         "focus:ring-indigo-500"]
          children))

(comment
  (button {:class "foo" :type "submit"} "Let's do this!"))
