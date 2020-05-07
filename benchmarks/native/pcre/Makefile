#
# General
#
#
CC=clang-4.0
CXX=clang++-4.0

SRC_DIR=../..

# Use local copy of Fuzzer
LIBFUZZER_SRC=$(SRC_DIR)/Fuzzer
LIBFUZZER_LIB=$(LIBFUZZER_SRC)/libFuzzer.a

DBGFLAGS=-g -ggdb3
CXXFLAGS=-O2 -Wall -std=c++11 -Wno-unused-variable $(DBGFLAGS)
COV_FLAGS=-fsanitize-coverage=edge,trace-pc-guard
FF=1

INC_PCRE= `pkg-config pcre_build/lib/pkgconfig/libpcre2-8.pc  --cflags --libs`
LD_PCRE= -Lpcre_build/lib -lpcre2-8 -lpcre2-posix


.PHONY:all
all: prelim regex input

prelim:
	rm -f regex input abc

# make our modified version of the Fuzzer
fuzzer:
	$(CXX) -c -g -O2 -std=c++11 $(LIBFUZZER_SRC)/*.cpp -I$(LIBFUZZER_SRC)
	ar rv $(LIBFUZZER_SRC)/libFuzzer.a Fuzzer*.o
	rm Fuzzer*.o

regex: driver.cpp
	$(CXX) $(CXXFLAGS) $(COV_FLAGS) $(INC_PCRE) $< $(LIBFUZZER_LIB) $(LD_PCRE) -o $@

abc: abcdriver.cpp
	rm -f abc
	$(CXX) $(CXXFLAGS) $(COV_FLAGS) $(INC_PCRE) $< $(LIBFUZZER_LIB) $(LD_PCRE) -o $@

both: both.cpp
	rm -f both
	$(CXX) $(CXXFLAGS) $(COV_FLAGS) $(INC_PCRE) $< $(LIBFUZZER_LIB) $(LD_PCRE) -o $@

input: inputdriver.cpp
	$(CXX) $(CXXFLAGS) $(COV_FLAGS) $(INC_PCRE) $< $(LIBFUZZER_LIB) $(LD_PCRE) -o $@

.PHONY: intest
intest:
	rm -rf inout incorpus && mkdir -p incorpus inout && \
ASAN_OPTIONS=halt_on_error=0:coverage=1:coverage_order_pcs=1:coverage_counters=1 \
	./input incorpus/ \
	-artifact_prefix=inout/ -print_final_stats=1 \
	-detect_leaks=0 -rss_limit_mb=10000 -shuffle=0 \
	-runs=10000000 -only_ascii=1 -dict=indict.txt 2>outintest

.PHONY: inf
inf:
	# rm -rf corpus && mkdir -p corpus out_inf && ./seed.sh
	rm -rf out_inf corpus_inf && mkdir -p corpus_inf out_inf \
&& ASAN_OPTIONS=halt_on_error=0:coverage=1:coverage_order_pcs=1:coverage_counters=1 \
	./$(TARGET) corpus_inf/ \
	-artifact_prefix=out_inf/ -print_final_stats=1 \
	-detect_leaks=0 -rss_limit_mb=10000 -shuffle=0 \
	-runs=-1 -max_len=100 -death_mode=0 -only_ascii=1 -dict=dict.txt \
	1>out_inf.txt 2>out_inf.err
.PHONY:clean
clean:
	rm -rf *.o *.a input driver regex

.PHONY:cleanf
cleanf:
	rm $(LIBFUZZER_LIB)
