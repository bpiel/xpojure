(ns xpojure.routes.home
  (:require [xpojure.state :as s]
            [xpojure.layout :as layout]
            [compojure.core :as c :refer [GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [liberator.core :as libr]
            [liberator.representation :as lib-rep]))

(def ext->mime
  {"jpg" "image/jpeg"
   "png" "image/png"})

(defn parse-ext
  [filename]
  (->> filename
       (re-find #".*\.(.*)")
       second))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn get-albums
  [pass]
  (->> @s/config
       :albums
       keys
       (map str)))

(defn get-albums-handler
  [ctx]
  (let [{:keys [pass]} (get-in ctx [:request :params])]
    (lib-rep/ring-response {:body (get-albums pass)
                            :headers {"Content-Type" "application/json"}})))

(defn get-images
  [album]
  (->> @s/config
       :albums
       (#(get % album))
       :images-sorted
       (map :name)
       (map str)))

(defn get-album-handler
  [ctx]
  (let [{:keys [album]} (get-in ctx [:request :params])]
    (lib-rep/ring-response {:body (get-images album)
                            :headers {"Content-Type" "application/json"}})))

(defn get-image-file
  [album image]
  (-> @s/config
      (get-in  [:albums album :images-map image :path])
      (io/file)))

(defn get-image-file-handler
  [ctx]
  (let [{:keys [album image]} (get-in ctx [:request :params])
        mime-type (-> image parse-ext ext->mime)]
    (lib-rep/ring-response {:body (get-image-file album image)
                            :headers {"Content-Type" mime-type}})))

#_ (get-image-file "pics1" "20160428_114152.jpg")
#_ (get-images "pics1" nil)

(defn mk-data-resource
  [ok-handler]
  (libr/resource {:available-media-types ["application/json"]
                  :allowed-methods [:get]
                  :handle-ok ok-handler}))

(defn mk-image-resource
  [ok-handler]
  (libr/resource {:available-media-types ["image/jpeg"]
                  :allowed-methods [:get]
                  :handle-ok ok-handler}))

(c/defroutes home-routes

  (c/context "/assets/albums/:album" [album]
    (GET "/images/:image" [] (mk-image-resource get-image-file-handler))
    (GET "/thumbnails/:image" [] (mk-image-resource identity)))

  (c/context "/data" []
    (GET "/albums" [] (mk-data-resource get-albums-handler))
    (GET "/albums/:album" [] (mk-data-resource get-album-handler)))

  (GET "/" [] (home-page))
  (GET "/:pass" [] (home-page)))
