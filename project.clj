(defproject starlanes "0.2.0-SNAPSHOT"
  :description "A Nostalgic Reimplementation of the old Star Lanes BASIC Game"
  :url "http://github.com/clojusc/clj-starlanes"
  :license {:name "BSD"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.combinatorics "0.0.4"]]
  :plugins [[lein-exec "0.3.1"]
            [lein-kibit "0.1.2"]]
  :aot [starlanes.trader]
  :main starlanes.trader
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :profiles {
    :dev {
      :dependencies [[org.clojure/tools.namespace "0.2.3"]
                     [org.clojure/java.classpath "0.2.0"]]}
    :testing {
      :dependencies [[clj-http-fake "0.4.1"]
                     [leiningen "2.3.3"]]}})
