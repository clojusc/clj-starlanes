VERSION=$(shell (head -1 project.clj |awk '{print $$3}'|sed -e 's/"//g'))
PROJECT=starlanes
JAR=target/$(PROJECT)-$(VERSION).jar
STANDALONE_JAR=target/$(PROJECT)-$(VERSION)-standalone.jar
BIN_DIR=/usr/local/bin

clean:
	rm -rf target/*.jar

$(BIN_DIR)/lein-exec:
	wget https://raw.github.com/kumarshantanu/lein-exec/master/lein-exec
	chmod a+x lein-exec
	clear
	@echo "Preparing to move lein-exec into /usr/local/bin ..."
	@read
	sudo mv lein-exec /usr/local/bin

$(BIN_DIR)/lein-exec-p:
	wget https://raw.github.com/kumarshantanu/lein-exec/master/lein-exec-p
	chmod a+x lein-exec-p
	clear
	@echo "Preparing to move lein-exec and lein-exec-p into /usr/local/bin ..."
	@read
	sudo mv lein-exec-p /usr/local/bin

script-setup: $(BIN_DIR)/lein-exec $(BIN_DIR)/lein-exec-p

build: clean
	@lein compile
	@lein uberjar

repl:
	@lein repl

repl-debug:
	@lein with-profile +debug repl

dev:
	@lein run --development

run:
	@lein run

run-jar: build
	java -jar $(JAR)

run-jar-standalone: build
	java -jar $(STANDALONE_JAR)

kibit-only:
	-@lein kibit && echo "Source code gets +1 from kibit ..."

test-only:
	@lein all test

coverage-only:
	-@lein cloverage --text --html
	@cat target/coverage/coverage.txt
	@echo "body {background-color: #000; color: #fff;} \
	a {color: #A5C0F0;}" >> target/coverage/coverage.css

lint: kibit-only
	@lein eastwood "{:namespaces [:source-paths]}" && \
	echo "Source code gets +1 from eastwood ..."

lint-unused:
	@lein eastwood "{:linters [:unused-fn-args :unused-locals :unused-namespaces :unused-private-vars :wrong-ns-form] :namespaces [:source-paths]}"

lint-ns:
	@lein eastwood "{:linters [:unused-namespaces :wrong-ns-form] :namespaces [:source-paths]}"

check: kibit-only test-only coverage-only

upload:
	@lein deploy clojars
