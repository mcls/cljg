(ns cljg.ninegag
  (:require [cljg.caching :as caching]
            [net.cgrand.enlive-html :as enlive-html]
            [clj-http.client :as client]))

;; ----------------------------------------------------------------------------
;; Parse HTML
;; ----------------------------------------------------------------------------

(defn html-source [url]
  (let [cached  (get @caching/page-cache url)]
    (println (str "Requesting html-source: " url))
    (if (nil? cached)
      (caching/update-cache url (:body (client/get url)))
      cached)))

(defn parse-html-source [url]
  (enlive-html/html-snippet (html-source url)))

(defn select-img-attrs [img-tag]
  (select-keys (:attrs img-tag) [:alt :src]))

(defn contains-gif? [post]
  (let [animated-divs (enlive-html/select post [:div.badge-animated-container-animated])]
    (= 0 (count animated-divs))))

(defn parse-regular-image [post]
  (let [image (first (enlive-html/select post [:img]))]
      (select-img-attrs image)))

(defn parse-gif-image [post]
  (let [image (first (enlive-html/select post [:img]))
        animated-divs (enlive-html/select post [:div.badge-animated-container-animated])]
      {:alt (:alt (:attrs image))
       :src (:data-image (:attrs (first animated-divs)))}))

(defn extract-image-from-post [post]
  (let [image (first (enlive-html/select post [:img]))]
    (if (contains-gif? post)
      (parse-regular-image post)
      (parse-gif-image post))))

(defn extract-images [page]
  (let [posts (enlive-html/select page [:div.post-container])
        parsed (map extract-image-from-post posts)]
    ;; NSFW posts require login.
    ;; They're parsed as empty maps, so filter them here
    (remove empty? parsed)))

(defn extract-next-url [page]
  (let [link (first (enlive-html/select page [:a.badge-load-more-post]))]
    (get-in link [:attrs :href])))

(defn parse-page [next-path]
  (let [page (parse-html-source (str "http://9gag.com" next-path))]
    {:images (extract-images page)
     :next-url (extract-next-url page)}))
