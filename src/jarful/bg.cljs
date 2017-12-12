(ns jarful.bg
  (:require [goog.object :as g]))

(defn notify [m sender]
  (doto (. js/chrome -notifications)
    (.clear "jarful")
    (.create "jarful"
            #js{:type "basic"
                :title "Jarful"
                :iconUrl "icon128.png"
                :message (g/get m "message")
                :priority (g/get m "status")}
            (constantly nil))))

(.. js/chrome -runtime -onMessage (addListener notify))
