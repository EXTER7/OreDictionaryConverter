#!/bin/bash

if [ "$#" -lt 1 ] || ! [ -d "$1" ] 
then
  echo "Usage: $0 Path_to_Forge"
  exit 1
fi

rm -rf "$1/mcp/src/minecraft/exter"
rm -rf "$1/mcp/src/minecraft/assets/fodc"

if [ -d "$1/mcp/src/minecraft/net/minecraftforge/common" ]
then
  mkdir "$1/mcp/src/minecraft/exter"
  mkdir -p "$1/mcp/src/minecraft/assets/fodc"
  ln -sf $(pwd)"/src/exter/fodc" "$1/mcp/src/minecraft/exter/"
  ln -sf $(pwd)"/textures" "$1/mcp/src/minecraft/assets/fodc/"
  ln -sf $(pwd)"/build_fodc.sh" "$1/mcp/build_fodc.sh" 
  echo "$0: Setup complete."
  echo "$0: To build run './build_fodc.sh' in the forge/mcp directory ."
else
  echo "$0: Cannot find minecraft sources."
  exit 1
fi

