#include <iostream>
#include <cstdlib>
#include <cstring>

#define PCRE2_CODE_UNIT_WIDTH 8

#include <pcre2.h>

//#define DEBUG 0

using namespace std;

static char *str = 	"AN.8;R9MAoxKrQ$L2PAq@Nzd"
					"fvZOKf-uTTzuuwD95qp%@.Zn"
					"xgu]soqJDS++3qs=wGgls3CE"
					"fTjr/(E1ewLX6j#xQ2UJoP7j"
					"jQEm-L6BBRoiPD4k8BqZgU91"
					"Mwue'.gFqaNI.>x49GTbYo56"
					"kX6CD4.5xNRq0Y(9$aM4_PZC"
					"xx9l_*YiAD^G2ekW$mgMUBPY"
					"n*pS1~gIFi6Mt?Nm3|6t9NXz"
					"VgOVH`fO7IlG&qQEO<XM?Yvx";

extern "C"
int LLVMFuzzerTestOneInput(const uint8_t *Data, size_t Size)
{
	pcre2_code *re;
	PCRE2_SPTR pattern;
	PCRE2_SPTR subject;
	PCRE2_SIZE err_off;
	pcre2_match_data *match;
	int len, rc, err_no;
	char *buf;

	if (Size < 1)
		return 0;

	len = Size / sizeof(char);

	buf = (char *) malloc(sizeof(char) * (len + 1));
	strncpy(buf, (char *) Data, len);

	if (buf[len - 1] == '\n')
		buf[len - 1] = '\0';
	else
		buf[len] = '\0';

	pattern = (PCRE2_SPTR) buf;
	subject = (PCRE2_SPTR) str;

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
	rc = pcre2_match(re, subject, len, 0, 0, match, NULL);

	pcre2_match_data_free(match);
	pcre2_code_free(re); 

out:
	free(buf);

	return 0;
}


