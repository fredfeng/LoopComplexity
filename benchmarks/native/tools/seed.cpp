#include <iostream>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>

#define TYPE uint8_t

using namespace std;

int main(int argc, char **argv) {
	if (argc < 3) {
		cout << "Usage: " << argv[0] << " [filename] [num] [num] [num]..." << endl;
		return 0;
	}

	FILE *fp = fopen(argv[1], "wb");

	int i = 0;
	for (int i = 2; i < argc; i++) {
		TYPE temp = (TYPE) atoi(argv[i]); 
		fwrite(&temp, sizeof(TYPE), 1, fp);
	}

	cout << "Done." << endl;
	return 0;
}
