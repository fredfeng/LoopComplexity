package edu.utexas.stac.hash;

public class AspDotNetString implements TestString {
    private String str;

    public AspDotNetString(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AspDotNetString that = (AspDotNetString) o;

        return str.equals(that.str);
    }

    @Override
    public int hashCode() {
        int result = 5381;

        for (int i = 0; i < str.length(); ++i)
            result = (result << 5 + result) ^ str.charAt(i);

        return result;
    }
}
