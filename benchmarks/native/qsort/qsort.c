#include "qsort.h"

int part(TYPE *arr, int low, int high) {
	TYPE pivot = arr[high];

	int i = low;
	for (int j = low; j < high; j++) {
		if (arr[j] <= pivot) {
			TYPE temp = arr[i];
			arr[i] = arr[j];
			arr[j] = temp;
			i++;
		}
	}

	TYPE temp = arr[i];
	arr[i] = arr[high];
	arr[high] = temp;

	return i;
}

// low is index of first element, high is index of last element
void slowfuzz_qsort(TYPE *arr, int low, int high) {
	if (low < high) {
		int pivot = part(arr, low, high);
		slowfuzz_qsort(arr, low, pivot - 1);
		slowfuzz_qsort(arr, pivot + 1, high);
	}
}