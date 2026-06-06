#!/bin/bash

./gradlew build

cp ./build/libs/fs-patches.jar ~/.local/share/PrismLauncher/instances/paladium/minecraft/mods
cp ./build/libs/fs-patches.jar ~/server/mods
