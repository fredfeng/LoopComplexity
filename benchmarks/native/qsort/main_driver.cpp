#include <cstdint>
#include <cstring>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <sys/stat.h>

using namespace std;

using type = uint8_t;

int part(type *arr, int low, int high) {
	type pivot = arr[high];

	int i = low;
	for (int j = low; j < high; j++) {
		if (arr[j] <= pivot) {
			type temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
			i++;
		}
	}

	type temp = arr[i];
	arr[i] = arr[high];
	arr[high] = temp;

	return i;
}

// low is index of first element, high is index of last element
void qsort(type *arr, int low, int high) {
	if (low < high) {
		int pivot = part(arr, low, high);
		qsort(arr, low, pivot - 1);
		qsort(arr, pivot + 1, high);
	}
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

	type *tmp = new type[Size];
	memcpy(tmp, Data, Size);

	qsort(tmp, 0, Size / sizeof(*tmp) - 1);

  delete[] tmp;
  free(Data);
  return 0;
}
