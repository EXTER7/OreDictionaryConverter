#!/bin/bash

MODNAME=fodc
MODVERSION=1.4.1

rm -rf packed/*
if ./recompile.sh && ./reobfuscate_srg.sh
then
  mkdir -p "packed/exter"
  mkdir -p "packed/assets/"$MODNAME

  mkdir -p "packed/buildcraft"

  cp -r "reobf/minecraft/exter/"$MODNAME "packed/exter/"
  cp -r "src/minecraft/assets/"$MODNAME"/"* "packed/assets/$MODNAME/"
  cp $MODNAME"_mcmod.info" "packed/mcmod.info"

  cd packed
  ZIPFILE=$MODNAME"-"$MODVERSION".zip"
  zip -r $ZIPFILE *
  mv $ZIPFILE "../"
  cd .. 
  echo "$0: Build complete, '"$ZIPFILE"' generated."
else
  echo "$0: Compile failed, aborting build."
  exit 1
fi
