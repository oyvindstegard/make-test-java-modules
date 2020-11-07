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
# Use `make test` to launch unit tests.


# Compiler and flags
JAVA = java
JAVAC = javac
JFLAGS = -encoding utf-8

# Directories
SRCDIR = src/main/java
SRCTESTDIR = src/test/java
TARGETDIR = target
CLASSESDIR = $(TARGETDIR)/classes

# Library dependencies (download rules come later).
# Must be complete including all transitive dependencies.
DEPS += org.junit.jupiter:junit-jupiter-api:5.7.0
DEPS += org.junit.jupiter:junit-jupiter-engine:5.7.0
DEPS += org.junit.platform:junit-platform-engine:1.7.0
DEPS += org.apiguardian:apiguardian-api:1.1.0
DEPS += org.opentest4j:opentest4j:1.2.0
DEPS += org.junit.platform:junit-platform-commons:1.7.0
DEPS += org.junit.platform:junit-platform-reporting:1.7.0
DEPS += org.junit.platform:junit-platform-launcher:1.7.0
DEPS += org.junit.platform:junit-platform-console:1.7.0
DEPS += org.mockito:mockito-core:3.6.0
DEPS += net.bytebuddy:byte-buddy:1.10.17        # required by Mockito
DEPS += net.bytebuddy:byte-buddy-agent:1.10.17  # required by Mockito
DEPS += org.objenesis:objenesis:3.1             # required by Mockito

grpId = $(word 1,$(subst :, ,$(1)))
artId = $(word 2,$(subst :, ,$(1)))
version =  $(word 3,$(subst :, ,$(1)))

DEPJARS = $(foreach dep,$(DEPS),$(TARGETDIR)/$(call artId,$(dep))-$(call version,$(dep)).jar)

# Construct Java class/module path. Make is quirky wrt. replacing space with
# colon, so define literal space using a nested subst call.
MODULEPATH = $(subst $(subst ,, ),:,$(DEPJARS))

# Locate Java source code files under main and test
SRCFILES = $(shell find $(SRCDIR) -name '*.java' -print)
SRCTESTFILES = $(shell find $(SRCTESTDIR) -name '*.java' -print)

# Build main class files with a single javac invocation, using flag file, and
# order-only dep on target-dir. (In make 4.3+, it is possble to use the class
# files themselves as grouped target, using "$(CLASSFILES) &: .." and still only
# have make invoke javac once.)
$(TARGETDIR)/main-build.flag : $(SRCFILES) $(DEPJARS) | $(TARGETDIR)
	$(JAVAC) $(JFLAGS) -p $(MODULEPATH) -d $(CLASSESDIR) --source-path $(SRCDIR) $(SRCFILES)
	touch $@

# Build test classes, patch to make them part of main module
$(TARGETDIR)/test-build.flag : $(TARGETDIR)/main-build.flag $(SRCTESTFILES) $(DEPJARS) | $(TARGETDIR)
	$(JAVAC) $(JFLAGS) --module-path $(MODULEPATH):$(CLASSESDIR) -d $(CLASSESDIR) \
		--source-path $(SRCTESTDIR) \
		--patch-module net.stegard.make.java=src/test/java \
		$(SRCTESTFILES)
	touch $@

$(TARGETDIR):
	mkdir $@

# Dependency download rules. Notice that we use an order-only
# prerequisite on the target directory "|..." for these, since we don't want to
# re-download the jar if the dir timestamp is updated, but we want the dir to
# exist before downloading the jar.
REPO = https://repo1.maven.org/maven2
DEPURLS = $(foreach d,$(DEPS),\
	$(REPO)/$(subst .,/,$(call grpId,$(d)))/$(call artId,$(d))/$(call version,$(d))/$(call artId,$(d))-$(call version,$(d)).jar)

$(DEPJARS): | $(TARGETDIR)
	curl -H "Accept: application/java-archive" "$(filter %$(notdir $@),$(DEPURLS))" -o $@

# Execute tests using JUnit ("@" in front disables command echoing)
test: $(TARGETDIR)/test-build.flag
	@echo List of Java modules:
	@$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR) --list-modules
	@echo Description of net.stegard.make.java module:
	@$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR) -d net.stegard.make.java
	@echo Launching tests ..
	$(JAVA) --module-path $(MODULEPATH):$(CLASSESDIR) --add-modules ALL-MODULE-PATH \
		org.junit.platform.console.ConsoleLauncher --disable-banner --select-module net.stegard.make.java

# Clean up
clean:
	rm -rf $(TARGETDIR)

.PHONY: test clean
