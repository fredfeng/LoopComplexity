#pragma once

#include <stddef.h>
#include <stdint.h>

// Require the definition of TYPE to compile

// Rename to slowfuzz_qsort to avoid naming conflict with qsort from libc
void slowfuzz_qsort(TYPE* arr, int low, int high);