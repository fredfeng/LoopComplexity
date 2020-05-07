package edu.utexas.stac.hash;

public class JavaString implements TestString {
    private String str;

    public JavaString(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaString that = (JavaString) o;

        return str.equals(that.str);
    }

    @Override
    public int hashCode() {
        int result = 0;

        for (int i = 0; i < str.length(); ++i)
            result = result * 31 + str.charAt(i);

        return result;
    }
}
