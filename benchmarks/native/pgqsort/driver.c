#include "ioutil.h"
#include "pgqsort.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Require the definition of TYPE to compile

int cmp(const void* a, const void* b) {
    return (*(TYPE*)a - *(TYPE*)b);
}

void test_qsort(uint8_t* data, size_t size) {
    TYPE* buf = (TYPE*)malloc(size);
    if (buf != NULL) {
        memcpy(buf, data, size);
        pg_qsort(buf, size / sizeof(TYPE), sizeof(TYPE), cmp);
        free(buf);
    }
}

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <input file>\n", argv[0]);
        return 0;
    }

    uint8_t* data;
    long size = read_file(argv[1], &data);
    if (size >= 0) {
        test_qsort(data, size);
        free(data);
    }
}
