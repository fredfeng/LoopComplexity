#include <cstdint>
#include <cstring>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <sys/stat.h>

#define TYPE uint8_t

using namespace std;

int cmp(const void *a, const void *b) {
	return ( *(TYPE * const *)a - *(TYPE * const *)b);
}

static int read_file(const char *fn, unsigned char **buf)
{
    struct stat file_status;
    FILE *fp;
    int ret = -1;

    if ((stat(fn, &file_status) != 0) ||
        ((fp = fopen(fn, "r")) == NULL) ||
        ((*buf = (unsigned char *)malloc(file_status.st_size)) == NULL)) {
        perror("read_file"); \
        return -1;
    }

    if (!fread(*buf, file_status.st_size, 1, fp)) {
        perror("read_file");
        free(*buf);
    } else {
        ret = file_status.st_size;
    }

    fclose(fp);
    return ret;
}

int main(int argc, char *argv[])
{
	uint8_t *Data;
	size_t Size = read_file(argv[1], &Data);

	TYPE *tmp = new TYPE[Size];
	memcpy(tmp, Data, Size);

	qsort(tmp, Size / sizeof(*tmp), sizeof(*tmp), cmp);

  delete[] tmp;
  free(Data);
  return 0;
}
