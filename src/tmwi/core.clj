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

(defn is-critical-reached [val]
  (<= (read-bat-capacity) val))

(defn -main
  [& args]
  (at-at/every 5000
               #(when (is-critical-reached 85)
                  (let [mp3-stream (FileInputStream.
                                    (File. "/home/morrisseymarr/siren.mp3"))
                        mp3-player (Player. mp3-stream)]
                    (.play mp3-player)
                    (println "battery is reached critical level")))
               my-pool))
