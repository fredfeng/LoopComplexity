package edu.utexas.microbench.bsort;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BubbleSort {

    // https://en.wikipedia.org/wiki/Insertion_sort
    // Worst case: reverse-sorted array
    public static <T extends Comparable<T>> void sort(T elems[]) {
        int n = elems.length;
        do {
            int newN = 0;
            for (int i = 1; i < n; ++i) {
                if (elems[i-1].compareTo(elems[i]) > 0) {
                    T tmp = elems[i-1];
                    elems[i-1] = elems[i];
                    elems[i] = tmp;
                    newN = i;
                }
            }
            n = newN;
        } while (n != 0);
    }

    public static void main(String args[]) {
        Integer[] inputs = new Integer[args.length];
        for (int i = 0; i < args.length; ++i)
            inputs[i] = Integer.parseInt(args[i]);
        sort(inputs);
        System.out.println(String.join(", ", Arrays.stream(inputs).map(Object::toString).collect(Collectors.toList())));
    }
}
