#include <iostream>
#include <cstdlib>
#include <cstring>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <sys/stat.h>
#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>

#include "inputfile.h"
//#define DEBUG 0

using namespace std;


/**
 * Caller must free buffer.
 * Return -1 on error.
 */
static int read_str_from_file(const char *fn, char **buf)
{
    struct stat file_status;
    FILE *fp;
    char *buffer;


    if (stat(fn, &file_status) != 0){
        perror("ERROR: Could not stat or file does not exist");
    }

    if ((fp = fopen(fn, "r")) == NULL)
        return -1;


    buffer = (char *)calloc(1, file_status.st_size + 1);
    if (!fread(buffer, file_status.st_size, 1, fp)) {
        perror("ERROR: Could not read file");
        free(buffer);
        return -1;
    }

    *buf = buffer;
    fclose(fp);
    return file_status.st_size + 1;
}



extern "C"
int LLVMFuzzerTestOneInput(const uint8_t *Data, size_t Size)
{
  char *regex_str = NULL;
	pcre2_code *re;
	PCRE2_SPTR pattern;
	PCRE2_SPTR subject;
	PCRE2_SIZE err_off;
	pcre2_match_data *match;
	int len, rc, sz, err_no;
	char *buf;

	if (Size < 1)
		return 0;

  if ((sz = read_str_from_file(REGEX_INPUT_FILE, &regex_str)) == -1) {
    return 0;
  }
	buf = (char *) calloc(1, Size + 1);
	strncpy(buf, (char *) Data, Size);
  buf[Size] = '\0';

	pattern = (PCRE2_SPTR) regex_str;
	subject = (PCRE2_SPTR) buf;
	re = pcre2_compile(pattern, PCRE2_ZERO_TERMINATED, 0, &err_no, &err_off, NULL);

	if (re == NULL) {
#ifdef DEBUG
		PCRE2_UCHAR err_buf[256];
		pcre2_get_error_message(err_no, err_buf, sizeof(err_buf));

		fprintf(stderr,
				"PCRE compilation failure at offset %d: %s\n",
				(int) err_off,
				err_buf);
#endif
		goto out;
	}

	match = pcre2_match_data_create_from_pattern(re, NULL);
	rc = pcre2_match(re, subject, Size, 0, 0, match, NULL);

	pcre2_match_data_free(match);
	pcre2_code_free(re);

out:
//	free(buf);

	return 0;
}


