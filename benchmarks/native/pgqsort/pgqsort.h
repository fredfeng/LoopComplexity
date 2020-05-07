#pragma once

#include <stddef.h>
#include <stdint.h>

void pg_qsort(void* a, size_t n, size_t es,
              int (*cmp)(const void*, const void*));