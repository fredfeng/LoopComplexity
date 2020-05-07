#include "ioutil.h"
#include "qsort.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char** argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <input file>\n", argv[0]);
        return 0;
    }

    uint8_t* data = NULL;
    long size = read_file(argv[1], &data);
    if (data == NULL || size < 0) {
        perror("readfile");
        return -1;
    }

    TYPE* buf = (TYPE*)malloc(size);
    if (buf == NULL) {
        perror("buf");
        if (data != NULL)
            free(data);
        return -1;
    }

    memcpy(buf, data, size);
    slowfuzz_qsort(buf, 0, size / sizeof(TYPE) - 1);

    free(buf);
    free(data);
    return 0;
}
