package edu.utexas.microbench.ssort;

import java.util.*;
import java.util.stream.Collectors;

public class ShellSort {

    interface GapSequence {
        Iterator<Integer> get(int n);
    }

    private static final GapSequence SHELL_SEQUENCE = n -> {
        if (n <= 0)
            throw new IllegalArgumentException();
        List<Integer> seq = new ArrayList<>();
        while (n >= 1) {
            seq.add(n);
            n = n / 2;
        }
        return seq.iterator();
    };

    private static final GapSequence FRANK_LAZARUS_SEQUENCE = n -> {
        if (n <= 0)
            throw new IllegalArgumentException();
        List<Integer> seq = new ArrayList<>();
        do {
            n = 2 * (n / 4) + 1;
            seq.add(n);
        } while (n > 1);
        return seq.iterator();
    };

    private static final GapSequence HIBBARD_SEQUENCE = n -> {
        if (n <= 0)
            throw new IllegalArgumentException();
        List<Integer> seq = new ArrayList<>();
        int i = 1;
        while (true) {
            i = i * 2;
            int j = i - 1;
            if (j >= n)
                break;
            seq.add(j);
        }
        Collections.reverse(seq);
        return seq.iterator();
    };

    private static final GapSequence SEDGEWICK_SEQUENCE = n -> {
        if (n <= 0)
            throw new IllegalArgumentException();
        List<Integer> seq = new ArrayList<>();
        seq.add(1);
        int pow4 = 4, pow2 = 1;
        while (true) {
            int i  = pow4 + 3 * pow2 + 1;
            if (i >= n)
                break;
            seq.add(i);
            pow4 *= 4;
            pow2 *= 2;
        }
        Collections.reverse(seq);
        return seq.iterator();
    };

    public static <T extends Comparable<T>> void sort(T arr[], GapSequence gapSequence) {
        int n = arr.length;
        Iterator<Integer> gaps = gapSequence.get(n);
        while (gaps.hasNext()) {
            int gap = gaps.next();

            for (int i = gap; i < n; ++i) {
                T temp = arr[i];
                int j = i;
                for (; j >= gap && arr[j - gap].compareTo(temp) > 0; j -= gap) {
                    arr[j] = arr[j - gap];
                }
                arr[j] = temp;
            }
        }
    }

    public static <T extends Comparable<T>> void sortShellSeq(T arr[]) {
        sort(arr, SHELL_SEQUENCE);
    }
    public static <T extends Comparable<T>> void sortFrankLazarusSeq(T arr[]) {
        sort(arr, FRANK_LAZARUS_SEQUENCE);
    }
    public static <T extends Comparable<T>> void sortHibbardSeq(T arr[]) {
        sort(arr, HIBBARD_SEQUENCE);
    }
    public static <T extends Comparable<T>> void sortSedgewickSeq(T arr[]) {
        sort(arr, SEDGEWICK_SEQUENCE);
    }

    public static void main(String args[]) {
        Integer[] inputs = new Integer[args.length];
        for (int i = 0; i < args.length; ++i)
            inputs[i] = Integer.parseInt(args[i]);
        sortSedgewickSeq(inputs);
        System.out.println(String.join(", ", Arrays.stream(inputs).map(Object::toString).collect(Collectors.toList())));
    }
}
