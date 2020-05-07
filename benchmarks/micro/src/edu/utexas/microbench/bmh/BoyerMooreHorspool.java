package edu.utexas.microbench.bmh;

import java.util.Arrays;
import java.util.stream.Collectors;

// https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore%E2%80%93Horspool_algorithm
public class BoyerMooreHorspool {

    private static final int ALPHABET_SIZE = 256;

    private static int[] preprocess(String pattern) {
        int table[] = new int[ALPHABET_SIZE];

        int patLength = pattern.length();
        for (int i = 0; i < ALPHABET_SIZE; ++i) {
            table[i] = patLength;
        }
        for (int i = 0; i < pattern.length() - 1; ++i) {
            int index = pattern.charAt(i);
            table[index] = patLength - 1 - i;
        }

        return table;
    }

    public static int search(String needle, String haystack) {
        if (needle == null || needle.length() == 0)
            throw new IllegalArgumentException();
        int[] table = preprocess(needle);
        int skip = 0;
        int haystackLength = haystack.length();
        int needleLength = needle.length();

        while (haystackLength - skip >= needleLength) {
            int i = needleLength - 1;
            while (haystack.charAt(skip + i) == needle.charAt(i)) {
                if (i == 0)
                    return skip;
                i = i - 1;
            }
            skip = skip + table[haystack.charAt(skip + needleLength - 1)];
        }
        return -1;
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: <prog> <needle> <haystack>");
            System.exit(0);
        }
        System.out.println(search(args[0], args[1]));
    }
}
