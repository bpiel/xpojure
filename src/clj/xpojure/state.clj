(ns xpojure.state
  (:require [hara.time :as time]
            [xpojure.image :as image]))

(declare watcher)
(def config (atom {:ready false}))
(def path "./test/clj/xpojure/test/example")
(def albums-path (str path "/albums"))

(def run (atom false))
(def sleep-time 5000)

(defn check-latest-mod-recur
  []
  (->> path
       clojure.java.io/file
       file-seq
       (map #(.lastModified %))
       (apply max)))

(defn read-links-config []
  {})

(defn scan-dir-for-images
  [dir]
  (->> dir
       .listFiles
       (keep image/extract-image-file-info)))

(defn images->map
  [images]
  (->> images
       (map #(vector (:name %)
                     %))
       (into {})))

(defn images->sorted
  [images]
  (sort-by (comp :long :created-at)
           images))

(defn scan-album-dir
  [dir]
  (let [name (.getName dir)
        images (scan-dir-for-images dir)]
    [name {:name name
           :path (.getPath dir)
           :images-map (images->map images)
           :images-sorted (images->sorted images)}]))

(defn scan-albums []
  (->> albums-path
       clojure.java.io/file
       .listFiles
       (filter #(.isDirectory %))
       (map scan-album-dir)
       (into {})))


(defn rebuild-config []
  {:ready true
   :links (read-links-config)
   :albums (scan-albums)})

(defn build-and-set-config! [last-mod]
  (reset! config (assoc (rebuild-config)
                        :last-modified last-mod)))

#_ (build-and-set-config! 0)
#_ (start-watcher)
#_ (stop-watcher)

(defn start-watcher []
  (reset! run true)
  (def watcher (future (do (while @run
                             (Thread/sleep sleep-time)
                             (let [last-mod (check-latest-mod-recur)
                                   cfg' @config]
                               (when (or (-> cfg' :ready false?)
                                         (time/before (:last-modified cfg')
                                                      last-mod))
                                 (build-and-set-config! last-mod))))
                           :done))))

(defn stop-watcher [] (reset! run false))


(comment "
TODO
- image endpoint
- album endpoint
- albumS endpoint
- link file reader
- apply auth
- thumbnail maker
- UI
")
