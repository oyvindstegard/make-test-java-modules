# Demonstrates modular Java build and unit tests using GNU Make as build tool

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

I do not recommend using GNU Make to build Java projects in general. However, it
is blazingly fast once it works, compared to Maven or other tools :).

## Running

Download jar dependencies and compile the sources:

    make
        
Launch unit tests:

    make test
    
By default launches JUnit 5 with Java 9+ module selection, rather than old style
class path scanning. You can customize arguments to JUnit launcher by setting
variable `JUNIT_ARGS`.

Or launch module main class:

    make main
    
You can change to a different main class by setting variable `MAINCLASS` to a
fully qualified class name in the main module.


## Resources

http://openjdk.java.net/projects/jigsaw/doc/ModulesAndJavac.pdf

https://sormuras.github.io/blog/2018-09-11-testing-in-the-modular-world.html

https://nipafx.dev/five-command-line-options-hack-java-module-system

