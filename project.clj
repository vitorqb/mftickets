(defproject mftickets "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.9.0"]
                 [clojure.java-time "0.3.2"]
                 [conman "0.8.4"]
                 [cprop "0.1.15"]
                 [expound "0.8.2"]
                 [luminus-jetty "0.1.7"]
                 [luminus-migrations "0.6.6"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [metosin/muuntaja "0.6.6"]
                 [metosin/reitit "0.3.10"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.webjars/webjars-locator "0.38"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.18"]
                 [clj-http "3.10.0"]
                 [clojure.java-time "0.3.2"]
                 [org.clojure/core.match "0.3.0"]
                 [com.rpl/specter "1.1.3"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot mftickets.core

  :plugins [[lein-ancient "0.6.15"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "mftickets.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[pjstadig/humane-test-output "0.10.0"]
                                 [prone "2019-07-08"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]]
                  
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :source-paths ["env/test/clj"]}
   :profiles/dev {}
   :profiles/test {}}

  :aliases
  {"run-tests" ["with-profile" "+test" ["run" "-m" "mftickets.system/run-tests"]]})
