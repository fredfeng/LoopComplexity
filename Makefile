
# This makefile depends on the existence of environment variable CLANG_INSTR, which stores the path to the clang-instr tool.

BENCH_DIR=benchmarks/native
OUT_DIR=out/artifact/native
CINCLUDE_DIR=benchmarks/native/tools
CUTILSRCS=benchmarks/native/tools/ioutil.c

BZIP2_SRCS_DIR=${BENCH_DIR}/bzip2/bzip2-1.0.6/
PCRE2_SRCS_DIR=${BENCH_DIR}/pcre/pcre2-10.30/src

.PHONY:all
all: isort isort_int appleqsort appleqsort_int bsdqsort bsdqsort_int gnuqsort gnuqsort_int pgqsort pgqsort_int qsort qsort_int phphash pcre_str bzip

.PHONY:check-env
check-env:
ifndef CLANG_INSTR
    $(error CLANG_INSTR env variable must be defined to the path of the clang-instr tool)
endif

prelim: check-env
	mkdir -p ${OUT_DIR}

# Insertion sort
isort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/isort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t -b main,read_file

isort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/isort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t -b main,read_file

# Apple qsort
appleqsort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/appleqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t  -b main,read_file

appleqsort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/appleqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t  -b main,read_file

# BSD qsort
bsdqsort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/bsdqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t  -b main,read_file

bsdqsort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/bsdqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t  -b main,read_file

# Gnu qsort
gnuqsort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/gnuqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t -b main,read_file

gnuqsort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/gnuqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t -b main,read_file

# Netbsd qsort
pgqsort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/pgqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t  -b main,read_file

pgqsort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/pgqsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t  -b main,read_file

# Slowfuzz qsort
qsort: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/qsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint8_t  -b main,read_file

qsort_int: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/qsort/*.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -D TYPE=uint32_t  -b main,read_file

# Php hash
phphash: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/php_hash/zend_hash_str.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -b main,read_file,test_driver

# Pcre
pcre_str: prelim
	$(CLANG_INSTR) -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} ${PCRE2_SRCS_DIR} -D HAVE_CONFIG_H PCRE2_CODE_UNIT_WIDTH=8 -b main,read_file,regex_driver \
		${CUTILSRCS} \
		${PCRE2_SRCS_DIR}/pcre2_auto_possess.c \
		${PCRE2_SRCS_DIR}/pcre2_chartables.c \
		${PCRE2_SRCS_DIR}/pcre2_compile.c \
		${PCRE2_SRCS_DIR}/pcre2_config.c \
		${PCRE2_SRCS_DIR}/pcre2_context.c \
		${PCRE2_SRCS_DIR}/pcre2_dfa_match.c \
		${PCRE2_SRCS_DIR}/pcre2_error.c \
		${PCRE2_SRCS_DIR}/pcre2_find_bracket.c \
		${PCRE2_SRCS_DIR}/pcre2_jit_compile.c \
		${PCRE2_SRCS_DIR}/pcre2_maketables.c \
		${PCRE2_SRCS_DIR}/pcre2_match.c \
		${PCRE2_SRCS_DIR}/pcre2_match_data.c \
		${PCRE2_SRCS_DIR}/pcre2_newline.c \
		${PCRE2_SRCS_DIR}/pcre2_ord2utf.c \
		${PCRE2_SRCS_DIR}/pcre2_pattern_info.c \
		${PCRE2_SRCS_DIR}/pcre2_serialize.c \
		${PCRE2_SRCS_DIR}/pcre2_string_utils.c \
		${PCRE2_SRCS_DIR}/pcre2_study.c \
		${PCRE2_SRCS_DIR}/pcre2_substitute.c \
		${PCRE2_SRCS_DIR}/pcre2_substring.c \
		${PCRE2_SRCS_DIR}/pcre2_tables.c \
		${PCRE2_SRCS_DIR}/pcre2_ucd.c \
		${PCRE2_SRCS_DIR}/pcre2_valid_utf.c \
		${PCRE2_SRCS_DIR}/pcre2_xclass.c \
		${BENCH_DIR}/pcre/fuzz_str_driver.c

# Bzip2
bzip: prelim
	$(CLANG_INSTR) ${BENCH_DIR}/bzip2/driver.c ${BZIP2_SRCS_DIR}/blocksort.c ${BZIP2_SRCS_DIR}/huffman.c ${BZIP2_SRCS_DIR}/crctable.c ${BZIP2_SRCS_DIR}/randtable.c ${BZIP2_SRCS_DIR}/compress.c ${BZIP2_SRCS_DIR}/decompress.c ${BZIP2_SRCS_DIR}/bzlib.c ${CUTILSRCS} -o ${OUT_DIR}/$@ -I ${CINCLUDE_DIR} -b main,read_file

.PHONY:clean
clean:
	rm -rf ${OUT_DIR}
