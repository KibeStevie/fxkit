#!/usr/bin/env bash
# Build everything and start the showcase app.
# Why "install" first? With Java modules, the showcase needs the *built* fxkit-core jar
# (from ~/.m2), so we install core before running the showcase.
set -euo pipefail
cd "$(dirname "$0")"

mvn -q -DskipTests install
mvn -q -pl fxkit-showcase javafx:run
