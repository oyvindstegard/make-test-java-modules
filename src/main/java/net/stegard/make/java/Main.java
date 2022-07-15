package net.stegard.make.java;

import static net.stegard.make.java.Logger.*;

enum Builder {

    ANT("Apache Ant"),
    MAVEN("Apache Maven Project"),
    GRADLE("Gradle build tool"),
    MAKE("GNU Make");

    public final String description;

    private Builder(String description) {
        this.description = description;
    }

}

public class Main {

    static {
        log("Main class loaded");
    }
    
    public static void main(String...args) {
        log("Hello from main class. Run tests with 'make test'.");
    }

}
