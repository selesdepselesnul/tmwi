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
   :current-status (read-power "status")})

;;m =>
;;{ :val val
;;  :path path
;;  :status status
;;  :message message
;;  :comparer-f comparer-f
;;}
(defn check-power
  [{:keys [val path status message comparer-f]}]
  (let [{:keys [capacity current-status]} (get-power)]
    (when (and (comparer-f (read-string capacity)
                           (read-string val))  
               (= current-status status))
      (let [mp3-stream (FileInputStream.
                        (File. path))
            mp3-player (Player. mp3-stream)]
        (println message)
        (.play mp3-player)))))

(def cli-options
  [["-l" "--low-path low path" "low path"]
   ["-f" "--high-path high path" "high path"]
   ["-m" "--maximum maximum val" "maximum val"]
   ["-c" "--critical critical val" "ciritical val"]
   ["-p" "--period period" "period check"]
   ["-h" "--help"]])

(def powers (atom []))

(defn -main
  [& args]
  (clear-screen)
  (let [options (:options (cli/parse-opts args cli-options))
        low-path (:low-path options)
        high-path (:high-path options)
        critical (:critical options)
        maximum (:maximum options)
        period (:period options)
        swap-powers! (fn [x] (swap! powers conj x))]
    (when (not-any? nil? [low-path critical])
      (swap-powers! 
       { :val critical
         :path low-path
         :status "Discharging"
         :message "Battery is reached minimum level, please charge it"
         :comparer-f <=
       }))
    (when (not-any? nil? [high-path maximum])
      (swap-powers!
       { :val maximum
         :path high-path
         :status "Charging"
         :message "Battery is reached maximum level, please unplug power"
         :comparer-f >=
       }))
    (if (and (> (count @powers) 0)
             (not (nil? period)))
      (at-at/every
       (read-string period)
       #(doseq [x @powers]
          (check-power x)) 
       pool)
      (println "Please fill the argument"))))


