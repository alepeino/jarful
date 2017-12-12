(ns jarful.dev
  (:require [goog.object :as g]))

(defn send-message-shim [m]
  (js/console.log "Sent:" (g/get m "message"))
  (if-let [handler (-> js/chrome .-runtime .-onMessageHandler)]
    (handler m {})))

(defn add-listener-shim [handler]
  (-> js/chrome .-runtime (g/set "onMessageHandler" handler)))

(defn create-notification-shim [id m]
  (js/console.log "Notification sent" m)
  (js/alert (.-message m)))

(-> js/chrome .-runtime (g/set "sendMessage" send-message-shim))
(-> js/chrome .-runtime (g/set "onMessage" #js{:addListener add-listener-shim}))
(-> js/chrome (g/set "notifications" #js{:create create-notification-shim}))
