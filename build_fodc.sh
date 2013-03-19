#!/bin/sh
rm -rf packed/*
if ./recompile.sh && ./reobfuscate.sh
then
  mkdir -p "packed/exter"
  cp -r "reobf/minecraft/exter/fodc" "packed/exter"
  cp "src/minecraft/exter/fodc/"*.png "packed/exter/fodc"
  cd packed
  zip -r fodc.zip *
  mv fodc.zip "../"
  cd .. 
  echo "$0: Build complete, 'fodc.zip' generated."
else
  echo "$0: Compile failed, aborting build."
  exit 1
fi
