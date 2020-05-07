#pragma once

#include <stddef.h>
#include <stdint.h>

void bsdqsort(void* aa, size_t n, size_t es,
              int (*cmp)(const void*, const void*));