#!/bin/csh
set dir=`dirname $0`
set file=`dirname $1`/`basename $1 .s`
sparc-linux-as -g -Asparc $file.s -o $file.o
sparc-linux-ld -dynamic-linker /lib/ld-uClibc.so.0 -e start -lc $file.o $dir/runtime.o -o $file
