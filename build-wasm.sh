#!/bin/bash

set -e

echo "Building WASM distribution..."
./gradlew wasmJsBrowserDistribution --no-configuration-cache

echo "Build completed successfully!"
echo "Output: composeApp/build/dist/wasmJs/productionExecutable/"
