#pragma once

#include <stddef.h>
#include <stdint.h>

// Read binary data from a file with given name into a specified buffer.
// Return the file size if succeeded, and a negative number if anything fails.
// If the function succeeded, *buf contains a pointer that must be freed later
long read_file(const char* file_name, uint8_t** buf);

// Read string from a file with given name into a specified buffer.
// Return the file size if succeeded, and a negative number if anything fails.
// If the function succeeded, *buf contains a pointer that must be freed later
long read_file_str(const char* file_name, char** buf);