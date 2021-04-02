package com.blacknebula.testcherry.testframework;

/**
 * @author Yanislav Mihaylov
 */
public enum NamingConvention {
    CAMEL_CASE_NAMING("Camel Case"),
    SNAKE_CASE_NAMING("Snake Case");

    private final String name;

    NamingConvention(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
