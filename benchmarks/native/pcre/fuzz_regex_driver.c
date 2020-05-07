#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include "pcre2.h"
#include "ioutil.h"

static const char* strs[] = {
  "AN.8;R9MAoxKrQ$L2PAq@Nzd"
};

void regex_driver(uint8_t* data, size_t size, size_t idx) {
  pcre2_code *re;
  PCRE2_SPTR pattern;
  PCRE2_SPTR subject;
  PCRE2_SIZE err_off;
  pcre2_match_data *match;
  int len, rc, err_no;
  char *buf;

  if (size < 1)
    return;

  len = size / sizeof(char);

  buf = (char *) malloc(sizeof(char) * (len + 1));
  strncpy(buf, (char *) data, len);

  if (buf[len - 1] == '\n')
    buf[len - 1] = '\0';
  else
    buf[len] = '\0';

  pattern = (PCRE2_SPTR) buf;
  subject = (PCRE2_SPTR) strs[idx];

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
}

int main(int argc, char** argv) {
  if (argc < 3) {
    fprintf(stderr, "Usage: %s <input file> <regex id>\n", argv[0]);
    return 0;
  }

  long idx = strtol(argv[2], NULL, 10);
  if (errno != 0 || idx < 0 || idx >= sizeof(strs) / sizeof(const char*)) {
    fprintf(stderr, "Invalid regex id: %s\n", argv[2]);
    return 0;
  }

  uint8_t *data;
  long size = read_file(argv[1], &data);
  if (size >= 0) {
    regex_driver(data, size, idx);
    free(data);
  }
}