(ns jarful.script
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [goog.object :as g]
            [hickory.core :as h]
            [hickory.select :as s]))

(defn notify [m]
  (.. js/chrome -runtime (sendMessage (clj->js m))))

(defn to-clipboard [s]
  (let [node (js/document.createElement "input")]
    (g/set node "value" s)
    (-> node (g/get "style") (g/set "opacity" "0"))
    (.appendChild js/document.body node)
    (.select node)
    (let [ok (js/document.execCommand "copy")]
      (.removeChild js/document.body node)
      ok)))

(defn on-click [e]
  (when-let [dep (-> e .-target .-dataset (g/get "clojarsDependency"))]
    (doto e .stopPropagation .preventDefault)
    (if (to-clipboard dep)
      (notify {:status 0 :msg (str "Copied " dep " to clipboard!")})
      (notify {:status 1 :msg "Error"}))))

;;;

(defn add-dep-data [node dep]
  (doto node
    (.setAttribute "title" "Click to copy dependency to clipboard")
    (-> .-dataset (g/set "clojarsDependency" dep))
    (-> .-style (g/set "outline" "1px dashed mediumpurple"))
    (-> .-style (g/set "outline-offset" "1px"))))

(defn inner-text [hic]
  (let [hic (:content hic)]
    (if (string? (first hic))
      (first hic)
      (apply str (map inner-text hic)))))

(defn extract-dep [xml]
  (->> (s/select (s/tag :text) xml)
    reverse second inner-text))

(def proxy (partial str "https://cors-anywhere.herokuapp.com/"))

(defn fetch-dep-vector [node]
  (go (let [url (-> node .-dataset (g/get "canonicalSrc") proxy)
            response (<! (http/get url {:with-credentials? false}))]
        (->> response :body h/parse h/as-hickory extract-dep (add-dep-data node)))))

;;;

(defn badges-foreach [f]
  (.. js/document
    (querySelectorAll "img[data-canonical-src*=\"clojars\"]")
    (forEach f)))

(defn stop []
  (badges-foreach #(.removeEventListener % "click" on-click)))

(defn start []
  (badges-foreach #(doto %
                    (fetch-dep-vector)
                    (.addEventListener "click" on-click))))

(defonce init (start))
