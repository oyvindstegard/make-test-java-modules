# Demonstrates modular Java build and unit tests using GNU make as build tool

This silly experiment demonstrates use of GNU Make to compile modular Java
source code with unit tests and automatic download of Maven style dependency
specs. The project uses the Maven standard directory layout and has build and
runtime dependencies (testing frameworks). Requires a JDK 11+, GNU Make and
Unix-like environment to run. This is bare bones and no fancy Maven or IDE is
here to help with compiling and launching anything. As such, it works as a
sandbox for learning lower level stuff about `java`, `javac` and the module
system.

Purpose: experiment with GNU Make and java, and learn about some options of the
Java module system and how to get it working in conjunction with unit tests and
source code building in general.

## Running

Use `make` to download jar dependencies and compile the sources.
Use `make test` to launch unit tests.

It launches JUnit 5 with Java 9+ module selection, rather than old style class
path scanning.


## Resources

http://openjdk.java.net/projects/jigsaw/doc/ModulesAndJavac.pdf

https://sormuras.github.io/blog/2018-09-11-testing-in-the-modular-world.html

https://nipafx.dev/five-command-line-options-hack-java-module-system

