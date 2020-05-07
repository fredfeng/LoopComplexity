#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "pcre2.h"
#include "ioutil.h"

static char *str =  "AN.8;R9MAoxKrQ$L2PAq@Nzd"
          "fvZOKf-uTTzuuwD95qp%@.Zn"
          "xgu]soqJDS++3qs=wGgls3CE"
          "fTjr/(E1ewLX6j#xQ2UJoP7j"
          "jQEm-L6BBRoiPD4k8BqZgU91"
          "Mwue'.gFqaNI.>x49GTbYo56"
          "kX6CD4.5xNRq0Y(9$aM4_PZC"
          "xx9l_*YiAD^G2ekW$mgMUBPY"
          "n*pS1~gIFi6Mt?Nm3|6t9NXz"
          "VgOVH`fO7IlG&qQEO<XM?Yvx";

void regex_driver(uint8_t* data, size_t size) {
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
}

int main(int argc, char** argv) {
  if (argc < 2) {
    fprintf(stderr, "Usage: %s <input file>\n", argv[0]);
    return 0;
  }

  uint8_t *data;
  long size = read_file(argv[1], &data);
  if (size >= 0) {
    regex_driver(data, size);
    free(data);
  }
}