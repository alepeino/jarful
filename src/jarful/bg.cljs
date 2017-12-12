(ns jarful.bg
  (:require [goog.object :as g]))

(defn notify [m sender]
  (.. js/chrome
    -notifications
    (create #js{:type "basic"
                :title "Jarful"
                :iconUrl "icon128.png"
                :message (g/get m "message")
                :priority (g/get m "status")})))

(.. js/chrome -runtime -onMessage (addListener notify))
