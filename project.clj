(defproject starlanes "0.2.0-SNAPSHOT"
  :description "A Nostalgic Reimplementation of the old Star Lanes BASIC Game"
  :url "http://github.com/clojusc/clj-starlanes"
  :license {:name "BSD"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.3"]]
  :plugins [[lein-exec "0.3.6"]
            [lein-kibit "0.1.2"]]
  :aot [starlanes.trader]
  :main starlanes.trader
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :profiles {
    :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
    :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
    :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
    :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
    :dev {
      :dependencies [[org.clojure/tools.namespace "0.2.10"]
                     [org.clojure/java.classpath "0.2.3"]
                     [clj-http-fake "1.0.2"]
                     [leiningen "2.6.1"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.5:dev,1.6:dev,1.7:dev,1.8:dev"]})
