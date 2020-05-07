#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

#define TYPE uint8_t

using namespace std;

int main(int argc, char **argv) {
	if (argc != 2) {
		cout << "Usage: " << argv[0] << " [filename]" << endl;
		return 0;
	}

	FILE *fp = fopen(argv[1], "rb");
	fseek(fp, 0, SEEK_END);
	int len = ftell(fp) / sizeof(TYPE);
	rewind(fp);

	TYPE *arr = (TYPE *) malloc(len * sizeof(TYPE));
	fread(arr, sizeof(TYPE), len, fp); 

	int i = 0;
	while (i++ < len) {
		printf("%05u ", *arr++);

		if (i % 10 == 0) 
			cout << endl;
	}

	cout << endl;
	return 0;
}
