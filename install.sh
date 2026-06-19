#!/bin/bash

./gradlew build

cp ./build/libs/fspatches.jar ~/.local/share/PrismLauncher/instances/paladium/minecraft/mods
cp ./build/libs/fspatches.jar ~/server/mods
