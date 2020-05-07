#include "ioutil.h"
#include <stdio.h>
#include <stdlib.h>

long read_file(const char* fn, uint8_t** buf) {
    FILE* fp = fopen(fn, "rb");
    if (fp == NULL) {
        perror("open_file");
        return -1;
    }

    int ret_code = fseek(fp, 0, SEEK_END);
    if (ret_code != 0) {
        perror("seek_end");
        fclose(fp);
        return -2;
    }
    long fsize = ftell(fp);
    if (fsize < 0) {
        perror("ftell");
        fclose(fp);
        return -3;
    }
    ret_code = fseek(fp, 0, SEEK_SET);
    if (ret_code != 0) {
        perror("seek_set");
        fclose(fp);
        return -4;
    }

    uint8_t* buffer = (uint8_t*)malloc(fsize * sizeof(uint8_t));
    if (buffer == NULL) {
        perror("malloc");
        fclose(fp);
        return -5;
    }

    if (fread(buffer, fsize, 1, fp) < 1) {
        perror("fread");
        free(buffer);
        fclose(fp);
        return -6;
    }

    *buf = buffer;
    fclose(fp);
    return fsize;
}

long read_file_str(const char* fn, char** buf) {
    FILE* fp = fopen(fn, "rb");
    if (fp == NULL) {
        perror("open_file");
        return -1;
    }

    int ret_code = fseek(fp, 0, SEEK_END);
    if (ret_code != 0) {
        perror("seek_end");
        fclose(fp);
        return -2;
    }
    long fsize = ftell(fp);
    if (fsize < 0) {
        perror("ftell");
        fclose(fp);
        return -3;
    }
    ret_code = fseek(fp, 0, SEEK_SET);
    if (ret_code != 0) {
        perror("seek_set");
        fclose(fp);
        return -4;
    }

    char* buffer = (char*)malloc(fsize * sizeof(char) + 1);
    if (buffer == NULL) {
        perror("malloc");
        fclose(fp);
        return -5;
    }

    if (fread(buffer, fsize, 1, fp) < 1) {
        perror("fread");
        free(buffer);
        fclose(fp);
        return -6;
    }

    buffer[fsize] = 0;
    *buf = buffer;
    fclose(fp);
    return fsize;
}
