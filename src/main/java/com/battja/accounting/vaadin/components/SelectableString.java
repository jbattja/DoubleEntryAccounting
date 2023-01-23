package com.battja.accounting.vaadin.components;

public record SelectableString(String string) implements MultiSelectFilterable {

    @Override
    public String getFilterName() {
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SelectableString other)) {
            return false;
        }
        return other.toString().equals(this.string);
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (string != null) {
            result = 31 * string.hashCode() + result;
        }
        return result;
    }

    @Override
    public String toString() {
        return string;
    }
}
