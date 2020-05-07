#pragma once

#include <stddef.h>
#include <stdint.h>

// Added from glibc-2.25/stdlib/stdlib.h
typedef int (*__compar_d_fn_t)(const void*, const void*, void*);

void gnu_quicksort(void* const pbase, size_t total_elems, size_t size,
                   __compar_d_fn_t cmp, void* arg);