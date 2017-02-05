(set-env!
 :source-paths #{"src"}
 :dependencies '[[adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.2.0")

(bootlaces! +version+)

(task-options!
 pom {:project     'powerlaces/boot-cljs-devtools
      :version     +version+
      :description "Boot task to add Chrome DevTool enhancements for CLJS."
      :url         "https://github.com/boot-clj/boot-cljs-devtools"
      :scm         {:url "https://github.com/boot-clj/boot-cljs-devtools"}
      :license     {"MIT" "https://opensource.org/licenses/MIT"}})

(def snapshot? #(.endsWith +version+ "-SNAPSHOT"))

(deftask deploy []
  (comp
   (build-jar)
   (if (snapshot?)
     (push-snapshot)
     (push-release))))
