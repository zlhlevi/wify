SHELL := /bin/bash

APP_NAME := wify
VERSION := $(shell sed -n 's:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -n 1)
RELEASE_DIR := release
PACKAGE_NAME := $(APP_NAME)-$(VERSION)
PACKAGE_ROOT := $(RELEASE_DIR)/$(PACKAGE_NAME)
PACKAGE_FILE := $(RELEASE_DIR)/$(PACKAGE_NAME).tar.gz
BACKEND_JAR := wify-app/target/wify-app-$(VERSION).jar
FRONTEND_DIR := wify-web

.PHONY: start stop restart build build-backend build-frontend clean package

start:
	./start.sh

stop:
	./stop.sh

restart: stop start

build: build-backend build-frontend

build-backend:
	mvn -pl wify-app -am package -DskipTests

build-frontend:
	cd $(FRONTEND_DIR) && \
	if [ ! -d node_modules ]; then npm install; fi && \
	npm run build

clean:
	mvn clean
	rm -rf $(FRONTEND_DIR)/dist
	rm -rf $(RELEASE_DIR)
	rm -rf logs
	rm -rf run

package: build
	rm -rf $(PACKAGE_ROOT)
	mkdir -p $(PACKAGE_ROOT)/backend
	mkdir -p $(PACKAGE_ROOT)/frontend
	mkdir -p $(PACKAGE_ROOT)/config
	mkdir -p $(PACKAGE_ROOT)/scripts
	cp $(BACKEND_JAR) $(PACKAGE_ROOT)/backend/
	cp wify-app/src/main/resources/application.yml $(PACKAGE_ROOT)/config/
	cp start.sh stop.sh $(PACKAGE_ROOT)/scripts/
	cp -R $(FRONTEND_DIR)/dist $(PACKAGE_ROOT)/frontend/
	tar -czf $(PACKAGE_FILE) -C $(RELEASE_DIR) $(PACKAGE_NAME)
