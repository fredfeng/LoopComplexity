#include "isort.h"

void isort(TYPE *arr, int n) {
	for (int i = 0; i < n; i++) {
		for (int c = i - 1; c >= 0; c--) {
			if (arr[c] > arr[c + 1]) {
				TYPE temp = arr[c];
				arr[c] = arr[c + 1];
				arr[c + 1] = temp;
			}
			else
				break;
		}
	}
}