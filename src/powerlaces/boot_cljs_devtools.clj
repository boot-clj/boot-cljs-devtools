(ns powerlaces.boot-cljs-devtools
  {:boot/export-tasks true}
  (:require [boot.core          :as    boot]
            [boot.task.built-in :refer [repl]]
            [boot.util          :as    util]
            [clojure.java.io    :as    io]
            [clojure.string     :as    str]
            [dirac.agent.config :as    dirac-conf]))

(def ^:private deps '#{binaryage/devtools binaryage/dirac})

(defn- add-preloads! [in-file out-file dirac-disabled?]
  (let [preloads (if dirac-disabled?
                   ['devtools.preload]
                   ['devtools.preload 'powerlaces.boot-cljs-devtools.preload])
        spec (-> in-file slurp read-string)]
    (when (not= :nodejs (-> spec :compiler-options :target))
      (util/info
       "Adding :preloads %s to %s...\n"
       preloads (.getName in-file))
      (io/make-parents out-file)
      (-> spec
          (update-in [:compiler-options :preloads] #(into preloads %))
          pr-str
          ((partial spit out-file))))))

(defn- assert-deps []
  (let [current (->> (boot/get-env :dependencies)
                     (map first)
                     set)
        missing (remove current deps)]
    (if (seq missing)
      (util/warn (str "You are missing necessary dependencies for boot-cljs-repl.\n"
                      "Please add the following dependencies to your project:\n"
                      (str/join "\n" missing) "\n")))))

(defn- relevant-cljs-edn [prev fileset ids]
  (let [relevant (map #(str % ".cljs.edn") ids)
        f (if ids
            #(boot/by-path relevant %)
            #(boot/by-ext [".cljs.edn"] %))]
    (-> (boot/fileset-diff prev fileset)
        boot/input-files
        f)))

(defn- start-dirac! [config]
  (when-not (:disabled config)
    (boot.util/dbug "Starting Dirac...\n")
    (require 'dirac.agent)
    ((resolve 'dirac.agent/boot!) config)))

(def nrepl-defaults
  {:port 8230
   :server true
   :middleware ['dirac.nrepl.middleware/dirac-repl]})

(boot/deftask cljs-devtools
  "Add Chrome Devtool enhancements for ClojureScript development."
  [b ids        BUILD_IDS  #{str} "Only inject devtools into these builds (= .cljs.edn files)"
   n nrepl-opts NREPL_OPTS edn     "Options passed to boot's `repl` task."
   d dirac-opts DIRAC_OPTS edn     "Options passed to dirac."]
  (let [tmp (boot/tmp-dir!)
        prev (atom nil)
        nrepl-opts (cond-> (merge nrepl-defaults nrepl-opts)
                     (get-in dirac-opts [:nrepl-server :port]) (assoc :port (get-in dirac-opts [:nrepl-server :port])))
        dirac-opts (cond-> (or dirac-opts {})
                     (:port nrepl-opts) (assoc-in [:nrepl-server :port] (:port nrepl-opts)))
        start-dirac-once (delay (start-dirac! dirac-opts))]
    (util/dbug "Normalized nrepl-opts %s\n" nrepl-opts)
    (util/dbug "Normalize dirac-opts %s\n"dirac-opts)
    (assert-deps)
    (assert (= (:port nrepl-opts) (get-in dirac-opts [:nrepl-server :port]))
            (format "Nrepl's :port (%s) and Dirac's [:nrepl-server :port] (%s) are not the same."
                    (:port nrepl-opts) (get-in dirac-opts [:nrepl-server :port])))
    (comp
     (boot/with-pre-wrap fileset
       (doseq [f (relevant-cljs-edn @prev fileset ids)]
         (let [path (boot/tmp-path f)
               in-file (boot/tmp-file f)
               out-file (io/file tmp path)]
           (io/make-parents out-file)
           (add-preloads! in-file out-file (:disabled dirac-opts))))
       (reset! prev fileset)
       (-> fileset
           (boot/add-resource tmp)
           (boot/commit!)))
     (apply repl (mapcat identity nrepl-opts))
     (boot/with-pass-thru _
       @start-dirac-once))))

(comment
  (require '[powerlaces.boot-cljs-devtools :as dvt])
  (boot (dvt/cljs-devtools)))
