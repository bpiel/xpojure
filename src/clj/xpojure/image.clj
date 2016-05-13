(ns xpojure.image
  (:require [hara.time :as time])
  (:import [org.apache.commons.imaging Imaging]))

(defn get-metadata
  "Get metadata for a File."
  [^java.io.File file]
  (-> file
      (Imaging/getMetadata)))

(defn get-directory
  "Get directory of directory-type from metadata.
  See: TiffDirectoryConstants"
  [metadata directory-type]
  (-> metadata .getExif (.findDirectory directory-type)))

(defn get-directories
  "Read only directories present in metadata."
  [metadata]
  (let [directories (-> metadata .getExif .getDirectories)]
    (map #(get-directory metadata (.type %)) directories)))

(defn fields->map
  "Convert field entries to a clojure map."
  [fields]
  (into {} (map (fn [field]
                  [(.getTagName field) (.getValue field)]) fields)))

(defn get-fields
  "Give a directory, get all field entries."
  [directory]
  (into [] (.getDirectoryEntries directory)))

(defn metadata->map
  "Generate a clojure map of directories/data from metadata."
  [metadata]
  (->> metadata
       get-directories
       (map (fn [directory]
              [(.description directory)
               (-> directory
                   get-fields
                   fields->map)]))
       (into {})))

(defn get-date-time-from-meta
  [metadata-map]
  (-> metadata-map
      (get "Root")
      (get "DateTime")
      (time/parse "yyyy:MM:dd HH:mm:ss" {:type java.util.Date})
      (time/to-map {})))

(defn valid-image-type?
  [file]
  true ;;TODO  implement
  )

(defn extract-image-file-info
  [file]
  (when (valid-image-type? file)
    (let [metadata-map (-> file get-metadata metadata->map)]
      {:name (.getName file)
       :path (.getPath file)
       :created-at (get-date-time-from-meta metadata-map)
       :exif metadata-map})))
