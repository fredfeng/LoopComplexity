package edu.utexas.stac.hash;

public class PythonString implements TestString {
    private String str;

    public PythonString(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PythonString that = (PythonString) o;

        return str.equals(that.str);
    }

    @Override
    public int hashCode() {
        int result = str.isEmpty() ? 0 : (str.charAt(0) << 7);
        for (int i = 0; i < str.length(); ++i)
            result = (1000003 * result) ^ str.charAt(i);
        result = result ^ str.length();

        if (result == -1)
            return -2;
        else
            return result;
    }
}
