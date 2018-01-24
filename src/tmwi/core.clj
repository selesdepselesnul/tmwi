(ns tmwi.core
  (:gen-class)
  (:require [overtone.at-at :as at-at]
            [clojure.string :as str])
  (:import (java.io File FileInputStream)
           (javazoom.jl.player Player)))

(def pool (at-at/mk-pool))

(defn clear-screen []
  (print "\u001b[2J")
  (print "\u001B[0;0f"))

(defn read-power [type]
  (str/trim
   (slurp (str "/sys/class/power_supply/BAT1/" type))))

(defn get-power []
  {:capacity (read-power "capacity")
   :status (read-power "status")})

(defn check-power [critical-val sound-path]
  (let [{:keys [capacity status]} (get-power)]
    (if (and (<= (read-string capacity)
                 (read-string critical-val))
             (= status "Discharging"))
      (let [mp3-stream (FileInputStream.
                        (File. sound-path))
            mp3-player (Player. mp3-stream)]
        (println "Battery is reached critical level, please charge it")
        (.play mp3-player))
      (println "Safe and sweet"))
    (clear-screen)))

(defn -main
  [& args]
  (clear-screen)
  (if (= 2 (count args))
    (let [critical-val (first args)
          sound-path (second args)]
      (at-at/every
       5000
       #(check-power critical-val sound-path) 
       pool))
    (println "Please fill argument, tmwi [critical-value] [path-to-sound]")))
