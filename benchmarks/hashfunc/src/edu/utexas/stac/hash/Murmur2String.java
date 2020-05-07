package edu.utexas.stac.hash;

public class Murmur2String implements TestString {
    private String str;
    private int seed;

    public Murmur2String(String str, int seed) {
        this.str = str;
        this.seed = seed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Murmur2String that = (Murmur2String) o;

        return str.equals(that.str);
    }

    @Override
    public int hashCode() {
        int m = (0xc6a4a793 << 32) | 0x5bd1e995;
        int r = 24;
        int len = str.length();
        int h = seed ^ len;
        for (int i = 0; i < str.length(); ++i) {
            int k = str.charAt(i);
            k *= m;
            k ^= (k >>> r);
            k *= m;
            h *= m;
            h ^= k;
        }

        // Assume the input len is a multiple of 8, the switch block will not be executed
        h ^= (h >>> 13);
        h *= m;
        h ^= (h >>> 15);

        return h;
    }
}
