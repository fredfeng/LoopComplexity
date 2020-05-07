#include <iostream>
#include <cstdlib>
#include <cstring>

#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>

//#define DEBUG 0

using namespace std;

extern "C"
int LLVMFuzzerTestOneInput(const uint8_t *Data, size_t Size)
{
	pcre2_code *re;
	PCRE2_SPTR pattern;
	PCRE2_SPTR subject;
	PCRE2_SIZE err_off;
	pcre2_match_data *match;
	int len1, len2, rc, err_no;
	char *buf1, *buf2;

	if (Size < 10)
		return 0;

  len1 = 10;
	len2 = Size - len1;
  if (len2 <= 1)
    return 0;

	buf1 = (char *) calloc(sizeof(char), len1 - 1);
	strncpy(buf1, (char *) Data, len1 - 1);
	buf2 = (char *) calloc(sizeof(char), len2);
	strncpy(buf2, (char *) Data + len1 - 1, len2);

	pattern = (PCRE2_SPTR) buf1;
	subject = (PCRE2_SPTR) buf2;

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
	rc = pcre2_match(re, subject, len1 - 1, 0, 0, match, NULL);

	pcre2_match_data_free(match);
	pcre2_code_free(re);

out:
	free(buf1);
	free(buf2);

	return 0;
}


