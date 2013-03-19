#!/bin/sh

if [ "$#" -lt 1 ] || ! [ -d "$1" ] 
then
  echo "Usage: $0 Path_to_MCP"
  exit 1
fi

rm -rf "$1/src/minecraft/exter"

if [ -d "$1/src/minecraft/net/minecraftforge/common" ]
then
  mkdir "$1/src/minecraft/exter"
  ln -sf $(pwd)"/exter/fodc" "$1/src/minecraft/exter"
  ln -sf $(pwd)"/build_fodc.sh" "$1/build_fodc.sh" 
  echo "$0: Setup complete."
  echo "$0: To build run './build_fodc.sh' in '$(realpath $1)' ."
else
  echo "$0: Cannot find minecraft sources."
  exit 1
fi

