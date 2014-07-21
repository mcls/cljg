(ns cljg.caching)

(def page-cache
  (atom {}))

(defn update-cache [url body]
  "Cache URL"
  (let [is-home-url (boolean (re-find (re-pattern "9gag\\.com/?$") url))]
    ;; Never cache the home url
    (when (not is-home-url)
      (println (str "Caching URL content: " url))
      (swap! page-cache (fn [curr] (assoc curr url body))))
    body))
