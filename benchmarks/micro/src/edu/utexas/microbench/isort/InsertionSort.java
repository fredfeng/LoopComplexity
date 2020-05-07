package edu.utexas.microbench.isort;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InsertionSort {

    // https://en.wikipedia.org/wiki/Insertion_sort
    // Worst case: reverse-sorted array
    public static <T extends Comparable<T>> void sort(T elems[]) {
        int i = 1;
        int n = elems.length;
        while (i < n) {
            T x = elems[i];
            int j = i - 1;
            while (j >= 0 && elems[j].compareTo(x) > 0) {
                elems[j + 1] = elems[j];
                j = j - 1;
            }
            elems[j+1] = x;
            i = i + 1;
        }
    }

    public static void main(String args[]) {
        Integer[] inputs = new Integer[args.length];
        for (int i = 0; i < args.length; ++i)
            inputs[i] = Integer.parseInt(args[i]);
        sort(inputs);
        System.out.println(String.join(", ", Arrays.stream(inputs).map(Object::toString).collect(Collectors.toList())));
    }
}
