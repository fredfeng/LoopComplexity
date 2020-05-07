package edu.utexas.microbench.qsort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuickSort {

    interface PivotStrategy<T> {
        T doPivot(List<T> list);
    }

    private static <T extends Comparable<T>> void sortImpl(List<T> array, PivotStrategy<T> pivotStrategy) {
        if (array.size() <= 1)
            return;

        List<T> smaller = new ArrayList<>();
        List<T> equal = new ArrayList<>();
        List<T> greater = new ArrayList<>();

        T pivot = pivotStrategy.doPivot(array);
        for (T x: array) {
            int cmp = x.compareTo(pivot);
            if (cmp > 0) {
                greater.add(x);
            } else if (cmp == 0) {
                equal.add(x);
            } else {
                smaller.add(x);
            }
        }
        sortImpl(greater, pivotStrategy);
        sortImpl(smaller, pivotStrategy);

        array.clear();
        array.addAll(smaller);
        array.addAll(equal);
        array.addAll(greater);
    }

    private static <T extends Comparable<T>> void sortArray(T arr[], PivotStrategy<T> strategy) {
        List<T> list = new ArrayList<>(arr.length);
        list.addAll(Arrays.asList(arr));
        sortImpl(list, strategy);
        for (int i = 0; i < list.size(); ++i)
            arr[i] = list.get(i);
    }

    public static <T extends Comparable<T>> void sortPivotFirst(T arr[]) {
        sortArray(arr, list -> list.get(0));
    }
    public static <T extends Comparable<T>> void sortPivotLast(T arr[]) {
        sortArray(arr, list -> list.get(list.size()-1));
    }
    public static <T extends Comparable<T>> void sortPivotMiddle(T arr[]) {
        sortArray(arr, list -> list.get(list.size() / 2));
    }

    public static void main(String args[]) {
        Integer[] inputs = new Integer[args.length];
        for (int i = 0; i < args.length; ++i)
            inputs[i] = Integer.parseInt(args[i]);
        sortPivotFirst(inputs);
        System.out.println(String.join(", ", Arrays.stream(inputs).map(Object::toString).collect(Collectors.toList())));
    }
}
