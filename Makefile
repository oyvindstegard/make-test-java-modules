# This silly experiment demonstrates use of GNU make to compile modular Java
# source code with unit tests and automatic download of Maven style dependency
# specs. The project uses the Maven standard directory layout and has build and
# runtime dependencies (testing frameworks). Requires a JDK 11+, GNU Make and
# Unix-like environment to run. This is bare bones and no fancy Maven or IDE is
# here to help with compiling and launching anything. As such, it works as a
# sandbox for learning lower level stuff about `java`, `javac` and the module
# system.

# Purpose: experiment with GNU Make and java, and learn about some options of the
# Java module system and how to get it working in conjunction with unit tests and
# source code building in general.

# ## Running

# Use `make` to download jar dependencies and compile the sources.
# Use `make test` to launch unit tests, optionally set JUNIT_ARGS to override test selection.
#                 Default is to run all tests found in module.
# Use `make main` to launch main class, optionally override main class with MAINCLASS variable.
#                 Default is 'MAINCLASS=net.stegard.make.java.Main'.
# Use `make modinfo` to show Java module runtime information.

# Compiler and flags
JAVA = java
JAVAC = javac
JFLAGS = -encoding utf-8

# Directories
SRCDIR = src/main/java
SRCTESTDIR = src/test/java
TARGETDIR = target
CLASSESDIR = $(TARGETDIR)/classes
TEST_CLASSESDIR = $(TARGETDIR)/test-classes

# Library dependencies (download rules come later).
# Must be complete including all transitive dependencies.
DEPS += org.junit.jupiter:junit-jupiter-api:5.9.3
DEPS += org.junit.jupiter:junit-jupiter-engine:5.9.3
DEPS += org.junit.platform:junit-platform-engine:1.9.3
DEPS += org.apiguardian:apiguardian-api:1.1.2
DEPS += org.opentest4j:opentest4j:1.2.0
DEPS += org.junit.platform:junit-platform-commons:1.9.3
DEPS += org.junit.platform:junit-platform-reporting:1.9.3
DEPS += org.junit.platform:junit-platform-launcher:1.9.3
DEPS += org.junit.platform:junit-platform-console:1.9.3
DEPS += org.mockito:mockito-core:5.4.0
DEPS += net.bytebuddy:byte-buddy:1.14.5         # required by Mockito
DEPS += net.bytebuddy:byte-buddy-agent:1.14.5   # required by Mockito
DEPS += org.objenesis:objenesis:3.3             # required by Mockito

grpId = $(word 1,$(subst :, ,$(1)))
artId = $(word 2,$(subst :, ,$(1)))
version = $(word 3,$(subst :, ,$(1)))
spc2colon = $(subst $(subst ,, ),:,$(1))

DEPJARS = $(foreach dep,$(DEPS),$(TARGETDIR)/$(call artId,$(dep))-$(call version,$(dep)).jar)

# Java modules
MAIN_MODULE = net.stegard.make.java
MODULES = $(shell ls $(SRCDIR))
TEST_MODULE = net.stegard.make.tests

# Construct Java class/module path. Make is quirky wrt. space, so use
# spc2colon call to replace space with colon.
MODULEPATH = $(call spc2colon,$(DEPJARS))

# Locate Java source code files under main and test
SRCFILES = $(shell find $(SRCDIR) -type f -name '*.java' -print)
SRCTESTFILES = $(shell find $(SRCTESTDIR) -type f -name '*.java' -print)

# Build test classes, patch inn all main modules classes to make them internal part of
# test module.
# Note: since we patch in source code of app modules here, the compiled app modules in $(CLASSESDIR)
# do not need to be on the module path when compiling test classes.
$(TARGETDIR)/test-build.flag : $(TARGETDIR)/main-build.flag $(SRCTESTFILES) $(DEPJARS) | $(TARGETDIR)
	$(JAVAC) $(JFLAGS) --module-path $(MODULEPATH) -d $(TEST_CLASSESDIR) \
		--module-source-path $(SRCTESTDIR) \
        --patch-module $(TEST_MODULE)=$(call spc2colon,$(addprefix $(SRCDIR)/,$(MODULES))) \
		$(SRCTESTFILES)
	touch $@

# Build main class files with a single javac invocation, using flag file, and
# order-only dep on target-dir. (In make 4.3+, it is possble to use the class
# files themselves as grouped target, using "$(CLASSFILES) &: .." and still only
# have make invoke javac once.)
$(TARGETDIR)/main-build.flag : $(SRCFILES) $(DEPJARS) | $(TARGETDIR)
	$(JAVAC) $(JFLAGS) -p $(MODULEPATH) -d $(CLASSESDIR) --module-source-path $(SRCDIR) $(SRCFILES)
	touch $@

$(TARGETDIR):
	mkdir $@

modinfo: $(TARGETDIR)/test-build.flag
	@echo List of Java modules:
	@$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR):$(TEST_CLASSESDIR) --list-modules
	@echo
	@for module in $(MODULES) $(TEST_MODULE); do \
		echo ;\
		echo Description of module $$module: ;\
		$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR):$(TEST_CLASSESDIR) -d $$module ;\
	done

# Dependency download rules. Notice that we use an order-only
# prerequisite on the target directory "|..." for these, since we don't want to
# re-download the jar if the dir timestamp is updated, but we want the dir to
# exist before downloading the jar.
REPO = https://repo1.maven.org/maven2
DEPURLS = $(foreach d,$(DEPS),\
	$(REPO)/$(subst .,/,$(call grpId,$(d)))/$(call artId,$(d))/$(call version,$(d))/$(call artId,$(d))-$(call version,$(d)).jar)

$(DEPJARS): | $(TARGETDIR)
	curl -sS -H "Accept: application/java-archive" "$(filter %$(notdir $@),$(DEPURLS))" -o $@

# Execute tests using JUnit ("@" in front disables command echoing)
# Note: also need to patch in the main modules here
JUNIT_ARGS ?= --select-module $(TEST_MODULE)
test: $(TARGETDIR)/test-build.flag
	$(JAVA) --module-path $(MODULEPATH):$(TEST_CLASSESDIR) --add-modules ALL-MODULE-PATH \
		org.junit.platform.console.ConsoleLauncher --disable-banner $(JUNIT_ARGS)

# Execute main class in main module
MAINCLASS ?= net.stegard.make.java.Main
main: $(TARGETDIR)/test-build.flag
	$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR) --module $(MAIN_MODULE)/$(MAINCLASS)

# Clean up
clean:
	rm -rf $(TARGETDIR)

.PHONY: main test modinfo clean
