#!/bin/bash

MODNAME=fodc

if [ "$#" -lt 1 ] || ! [ -d "$1" ] 
then
  echo "Usage: $0 Path_to_Forge"
  exit 1
fi


if [ -d "$1/mcp/src/minecraft/net/minecraftforge/common" ]
then
  mkdir -p "$1/mcp/src/minecraft/exter"
  mkdir -p "$1/mcp/src/minecraft/assets"

  rm -rf "$1/mcp/src/minecraft/exter/"$MODNAME
  rm -rf "$1/mcp/src/minecraft/assets/"$MODNAME

  ln -sf $(pwd)"/src/exter/"$MODNAME "$1/mcp/src/minecraft/exter/"
  ln -sf $(pwd)"/assets/"$MODNAME "$1/mcp/src/minecraft/assets/"
  ln -sf $(pwd)"/build_"$MODNAME".sh" "$1/mcp/build_"$MODNAME".sh" 
  echo "$0: Setup complete."
  echo "$0: To build run './build_"$MODNAME".sh' in the forge/mcp directory ."
else
  echo "$0: Cannot find minecraft sources."
  exit 1
fi

