#include "bzip2-1.0.6/bzlib.h"
#include "ioutil.h"
#include <stdio.h>
#include <stdlib.h>

uint8_t *test_decompress(uint8_t *data, size_t size) {
  int len = size * 3;
  uint8_t *out_buffer = (uint8_t *)malloc(len);
  BZ2_bzBuffToBuffDecompress((char *)data,         /* destination buffer */
                             (unsigned int *)&len, /* dest buffer size */
                             (char *)data,         /* source buffer */
                             (unsigned int)size,   /* src buffer size */
                             0,  /* small memory footprint, 0 = fast */
                             0); /* verbosity, 0 = no debugging */
  return out_buffer;
}

int main(int argc, char **argv) {
  if (argc < 2) {
    fprintf(stderr, "Usage: %s <input file>\n", argv[0]);
    return 0;
  }

  uint8_t *data;
  long size = read_file(argv[1], &data);
  if (size >= 0) {
    uint8_t *out = test_decompress(data, size);
    free(data);
    free(out);
  }

  return 0;
}
