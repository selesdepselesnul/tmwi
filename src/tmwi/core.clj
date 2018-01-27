(ns tmwi.core
  (:gen-class)
  (:require [overtone.at-at :as at-at]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli])
  (:import (java.io File FileInputStream)
           (javazoom.jl.player Player)))

(def power-supply-path "/sys/class/power_supply/")
(def pool (at-at/mk-pool))

(defn clear-screen []
  (print "\u001b[2J")
  (print "\u001B[0;0f"))

(defn get-bat-dir []
  (->>
   (.listFiles (io/file power-supply-path))
   (map #(.toString %))
   (filter  #(re-find #"BAT.*" %))
   first))

(defn read-power [type]
  (str/trim
   (slurp (str (get-bat-dir) "/" type))))

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

(def cli-options
  [["-l" "--low-path low path" "Low Path"]
   ["-c" "--critical critical val" "ciritical val"]
   ["-p" "--period period" "period check"]
   ["-h" "--help"]])

(defn -main
  [& args]
  (clear-screen)
  (let [options (:options (cli/parse-opts args cli-options))
        low-path (:low-path options)
        critical (:critical options)
        period (:period options)]
    (if (some nil? [low-path critical period])
      (println "Please fill the argument")
      (at-at/every
       (read-string period)
       #(check-power critical low-path) 
       pool))))


