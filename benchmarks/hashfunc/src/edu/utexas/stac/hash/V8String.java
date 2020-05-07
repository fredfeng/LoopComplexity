package edu.utexas.stac.hash;

public class V8String implements TestString {
    private String str;

    public V8String(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        V8String v8String = (V8String) o;

        return str.equals(v8String.str);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < str.length(); ++i) {
            result += str.charAt(i);
            result += (result << 10);
            result ^= (result >>> 6);
        }
        result += (result << 3);
        result ^= (result >>> 11);
        result += (result << 15);
        if (result == 0)
            return 27;
        else
            return result;
    }
}
