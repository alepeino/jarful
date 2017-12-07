(ns jarful.bg
  (:require [goog.object :as g]))

(defn notify [m sender]
  (.. js/chrome
    -notifications
    (create "jarful" #js{:type "basic"
                         :title "Jarful"
                         :iconUrl "icon.png"
                         :message (g/get m "msg")
                         :priority (g/get m "status")})))

(.. js/chrome -runtime -onMessage (addListener notify))
