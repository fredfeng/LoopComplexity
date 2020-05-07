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

BZIP_SRC=bzip2-1.0.6
BZIP_LIB=$(BZIP_SRC)/libbz2.a

DBGFLAGS=-g -ggdb3
CXXFLAGS=-O0 -Wall -std=c++11 -Wno-unused-variable $(DBGFLAGS)
COV_FLAGS=-fsanitize-coverage=edge,trace-pc-guard

# Defaults for LibFuzzer
CORPUS=corpus/
OUTPUT=out/
RUNS=100000000
MAX_LEN=500

TARGET=driver
TARGET_SRC=driver.cpp

.PHONY:all
all: prelim $(TARGET)

prelim:
	rm -f $(TARGET)

# make our modified version of the Fuzzer
fuzzer:
	$(CXX) -c -g -O2 -std=c++11 $(LIBFUZZER_SRC)/*.cpp -I$(LIBFUZZER_SRC)
	ar rv $(LIBFUZZER_SRC)/libFuzzer.a Fuzzer*.o
	rm Fuzzer*.o

$(TARGET): $(TARGET_SRC)
	$(CXX) $(CXXFLAGS) $(COV_FLAGS) $< $(LIBFUZZER_LIB) $(BZIP_LIB) \
		-I$(BZIP_SRC) -o $@

# 300 million ~ 3 hours of runtime
# cp -r seeds $(CORPUS)
# cp $(UNIT) corpus
.PHONY: test
test:
	rm -rf $(CORPUS) && mkdir -p $(CORPUS) $(OUTPUT) && cp seeds/* $(CORPUS) \
		&& ASAN_OPTIONS=halt_on_error=0:coverage=1:coverage_order_pcs=1:coverage_counters=1 \
	./$(TARGET) $(CORPUS) \
	-artifact_prefix=$(OUTPUT) -print_final_stats=1 \
	-detect_leaks=0 -rss_limit_mb=10000 -shuffle=0 \
	-runs=$(RUNS) -max_len=$(MAX_LEN) 

zpipe: zpipe.c
	$(CC) $< zlib-zpipe/libz.a -Izlib-zpipe -o $@

zpipe-unmodified: zpipe.c
	$(CC) $< zlib-unmodified/libz.a -Izlib-unmodified -o $@

.PHONY:clean
clean:
	rm -rf *.o *.a $(TARGET) qtable.bin zpipe zpipe-unmodified

.PHONY:cleanf
cleanf:
	rm $(LIBFUZZER_LIB)
