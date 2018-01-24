(ns tmwi.core
  (:gen-class)
  (:require [overtone.at-at :as at-at])
  (:import (java.io File FileInputStream)
           (javazoom.jl.player Player)))

(def my-pool (at-at/mk-pool))

(defn read-bat-capacity []
  (read-string
   (clojure.string/replace
    (slurp "/sys/class/power_supply/BAT1/capacity")
           #"\n"
           "")))

(def mp3-stream (FileInputStream. (File. "/home/morrisseymarr/siren.mp3")))
(def mp3-player (Player. mp3-stream))

(defn is-critical-reached [val]
  (<= (read-bat-capacity) val))

(defn -main
  [& args]
  (at-at/every 5000
               #(when (is-critical-reached 66)
                  (.play mp3-player)
                  (at-at/stop-and-reset-pool! my-pool))
               my-pool))
