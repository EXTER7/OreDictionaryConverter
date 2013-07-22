#!/bin/bash
rm -rf packed/*
if ./recompile.sh && ./reobfuscate.sh
then
  mkdir -p "packed/exter"
  mkdir -p "packed/assets/fodc/textures"
  cp -r "reobf/minecraft/exter/fodc" "packed/exter/"
  cp -r "src/minecraft/assets/fodc/textures/"* "packed/assets/fodc/textures/"
  cd packed
  zip -r fodc.zip *
  mv fodc.zip "../"
  cd .. 
  echo "$0: Build complete, 'fodc.zip' generated."
else
  echo "$0: Compile failed, aborting build."
  exit 1
fi
