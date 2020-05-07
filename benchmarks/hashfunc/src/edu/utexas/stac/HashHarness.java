package edu.utexas.stac;

import edu.utexas.stac.collection.Hashtable;
import edu.utexas.stac.hash.*;

import java.util.Arrays;

public class HashHarness {
    private static <T extends TestString> int hashHarness(T inputStrings[]) {
        // Make sure resize is not an issue
        int tableSize = (int)(inputStrings.length / (double)Hashtable.DEFAULT_LOAD_FACTOR) + 1;

        // Insert all strings into the hash table
        Hashtable<TestString, T> table = new Hashtable<>(tableSize);
        for (T inputString: inputStrings) {
            table.put(inputString, inputString);
        }

        // Make sure that table operations do not get optimized out
        return table.size();
    }

    public static int phpStringHarness(String inputStrings[]) {
        PhpString[] strArray = Arrays.stream(inputStrings).map(PhpString::new).toArray(PhpString[]::new);
        return hashHarness(strArray);
    }
    public static int javaStringHarness(String inputStrings[]) {
        JavaString[] strArray = Arrays.stream(inputStrings).map(JavaString::new).toArray(JavaString[]::new);
        return hashHarness(strArray);
    }
    public static int rubyStringHarness(String inputStrings[]) {
        RubyString[] strArray = Arrays.stream(inputStrings).map(RubyString::new).toArray(RubyString[]::new);
        return hashHarness(strArray);
    }
    public static int aspDotNetStringHarness(String inputStrings[]) {
        AspDotNetString[] strArray = Arrays.stream(inputStrings).map(AspDotNetString::new).toArray(AspDotNetString[]::new);
        return hashHarness(strArray);
    }
    public static int pythonStringHarness(String inputStrings[]) {
        PythonString[] strArray = Arrays.stream(inputStrings).map(PythonString::new).toArray(PythonString[]::new);
        return hashHarness(strArray);
    }
    public static int v8StringHarness(String inputStrings[]) {
        V8String[] strArray = Arrays.stream(inputStrings).map(V8String::new).toArray(V8String[]::new);
        return hashHarness(strArray);
    }
    public static int murmur2StringHarness(String inputStrings[], int seed) {
        Murmur2String[] strArray = Arrays.stream(inputStrings).map(s -> new Murmur2String(s, seed)).toArray
                (Murmur2String[]::new);
        return hashHarness(strArray);
    }
}
