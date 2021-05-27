#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void *alloc_object(int size){
	return memset(malloc(size), 0, size);
}

void *new_arr(int count, int size){
	void *ret = calloc(count + 1, size);
	((int *) ret)[0] = count;
	return ret;	
}

void print_int(int i){
	printf("%i\n", i);
}