package slowfuzz;

public class InsertionSort {

    // insert key into array[0..j]
    private static void insert(int key, int array[], int j) {
        int i = j - 1;
        while (i >= 0 && array[i] > key) {
            array[i+1] = array[i];
            i = i - 1;
        }
        array[i+1] = key;
    }

    // insertion sort algorithm from CLRS
    private static void sort(int array[]) {
        for (int j = 1; j < array.length; ++j) {
            int key = array[j];
            insert(key, array, j);
        }
    }

    public static void sortPublic(int array[]) {
        sort(array);
    }

    // parse input numbers
    private static int[] parseInput(String args[]) {
        int inputLen = args.length;
        int inputArray[] = new int[inputLen];
        for (int i = 0; i < inputLen; ++i)
            inputArray[i] = Integer.parseInt(args[i]);
        return inputArray;
    }

    public static void main(String args[]) {
        sort(parseInput(args));
    }
}
