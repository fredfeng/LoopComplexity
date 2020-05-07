#!/bin/bash
f=512/exponential
echo "#ifndef __INPUT_FILE_H__" > inputfile.h
echo "#define __INPUT_FILE_H__" >> inputfile.h
echo "#define REGEX_INPUT_FILE \"${f}\"" >> inputfile.h
echo "#endif" >> inputfile.h
make 1>/dev/null 2>&1
echo "Processing $f"
outdir=512_out/exponential
rm -rf incorpus512 && mkdir -p $outdir incorpus512 &&
cp input $outdir/

for (( i = 30; i < 40; i++ )); do
    for (( max_len=1; max_len<=1024; max_len*=2 )) do
        if [[ $max_len -eq 1024 ]]; then
            runs=4096
        else
            runs=1000000
        fi
        echo "Running exponential ($i:$max_len)"
        rm -rf incorpus512 && mkdir -p $outdir incorpus512
        echo "aaab" > incorpus512/1
        echo "ab" > incorpus512/2
        ASAN_OPTIONS=halt_on_error=0:coverage=1:coverage_order_pcs=1:coverage_counters=1 \
        ./input incorpus512/ -artifact_prefix=$outdir/ -print_final_stats=1 \
        -detect_leaks=0 -rss_limit_mb=10000 -max_len=$max_len -shuffle=0 \
        -runs=$runs -only_ascii=1 -only_dict=1 -dict=indict512exp.txt 2>$outdir/out${max_len}_${i}.txt
    done
done



f=512/superlinear
echo "#ifndef __INPUT_FILE_H__" > inputfile.h
echo "#define __INPUT_FILE_H__" >> inputfile.h
echo "#define REGEX_INPUT_FILE \"${f}\"" >> inputfile.h
echo "#endif" >> inputfile.h
rm -f input
make 1>/dev/null 2>&1
echo "Processing $f"
outdir=512_out/superlinear
cp input $outdir/
for (( i = 13; i < 23; i++ )); do
    for (( max_len=1; max_len<=1024; max_len*=2 )) do
        if [[ $max_len -eq 1024 ]]; then
            runs=4096
        else
            runs=1000000
        fi
        echo "Running superlinear ($i:$max_len)"
        rm -rf incorpus512 && mkdir -p $outdir incorpus512
        echo "cacbcc" > incorpus512/1
        echo "abc" > incorpus512/2
        ASAN_OPTIONS=halt_on_error=0:coverage=1:coverage_order_pcs=1:coverage_counters=1 \
        ./input incorpus512/ -artifact_prefix=$outdir/ -print_final_stats=1 \
        -detect_leaks=0 -rss_limit_mb=10000 -max_len=$max_len -shuffle=0 \
        -runs=$runs -only_ascii=1 -only_dict=1 -dict=indict512.txt 2>$outdir/out${max_len}_${i}.txt
    done
done
