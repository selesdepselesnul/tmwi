(ns tmwi.core
  (:gen-class)
  (:require [overtone.at-at :as at-at])
  (:import (javafx.scene.media Media)
           (javafx.scene.media MediaPlayer)))

(def my-pool (at-at/mk-pool))

(defn read-bat-capacity []
  (read-string
   (clojure.string/replace
    (slurp "/sys/class/power_supply/BAT1/capacity")
           #"\n"
           "")))

(defn is-critical-reached [val]
  (<= (read-bat-capacity) val))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (at-at/every 5000 #(println (is-critical-reached 23)) my-pool))
