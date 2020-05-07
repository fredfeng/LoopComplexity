package slowfuzz;

import java.util.List;
import java.util.ArrayList;

public class QuickSort {

    // quick sort algorithm from fig 1 in SlowFuzz paper
    private static void sort(List<Integer> array) {
        if (array.size() <= 1)
            return;

        List<Integer> smaller = new ArrayList<>();
        List<Integer> equal = new ArrayList<>();
        List<Integer> greater = new ArrayList<>();

        Integer pivot = array.get(0);
        for (Integer x: array) {
            if (x > pivot) {
                greater.add(x);
            } else if (x.equals(pivot)) {
                equal.add(x);
            } else {
                smaller.add(x);
            }
        }
        sort(greater);
        sort(smaller);

        array.clear();
        array.addAll(smaller);
        array.addAll(equal);
        array.addAll(greater);
    }

    private static void sortMiddlePivot(List<Integer> array) {
        if (array.size() <= 1)
            return;

        List<Integer> smaller = new ArrayList<>();
        List<Integer> equal = new ArrayList<>();
        List<Integer> greater = new ArrayList<>();

        Integer pivot = array.get(array.size() / 2);
        for (Integer x: array) {
            if (x > pivot) {
                greater.add(x);
            } else if (x.equals(pivot)) {
                equal.add(x);
            } else {
                smaller.add(x);
            }
        }
        sortMiddlePivot(greater);
        sortMiddlePivot(smaller);

        array.clear();
        array.addAll(smaller);
        array.addAll(equal);
        array.addAll(greater);
    }

    public static void sortPublic(int array[]) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i: array)
            list.add(i);
        sort(list);
    }

    public static void sortMiddlePivotPublic(int array[]) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i: array)
            list.add(i);
        sortMiddlePivot(list);
    }

    // parse input numbers
    private static List<Integer> parseInput(String args[]) {
        int inputLen = args.length;
        List<Integer> inputList = new ArrayList<>(inputLen);
        for (String arg: args)
            inputList.add(Integer.parseInt(arg));
        return inputList;
    }

    public static void main(String args[]) {
        sortMiddlePivot(parseInput(args));
    }
}
