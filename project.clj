(defproject pubnub-cljs "0.1.0-SNAPSHOT"
  :description "Idiomatic ClojureScript API around cljs/pubnub"
  :url "https://github.com/karolmajta/pubnub-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "https://github.com/karolmajta/pubnub-cljs"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljsjs/pubnub "3.7.15-0"]
                 [funcool/promesa "0.5.0"]]

  :source-paths ["src/cljs"]

  :profiles {
    :dev {
      :resource-paths ["example/resources"]
      :clean-targets ^{:protect false} ["example/resources/public/js/compiled" "target"]
      :dependencies [[org.clojure/clojure "1.7.0"]
                     [org.clojure/clojurescript "1.7.122"]
                     [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
      :plugins [[lein-cljsbuild "1.1.0"]
                [lein-figwheel "0.4.1"]]
      :cljsbuild {
        :builds {:dev {
                  ;; althought "src/cljs" is already set as source path outside
                  ;; of dev profile we need to repeat it in cljsbuild's source-paths
                  ;; so that figwheel will pick up changes.
                  :source-paths ["src/cljs" "example/cljs" "example/clj"]
                  :figwheel { :on-jsload "pubnub-cljs-example.main/on-js-reload" }
                  :compiler {:main pubnub-cljs-example.main
                             :asset-path "js/compiled/out"
                             :output-to "example/resources/public/js/compiled/app.js"
                             :output-dir "example/resources/public/js/compiled/out"
                             :source-map-timestamp true }}}}}})
