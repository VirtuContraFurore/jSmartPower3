PROG=release/jSmartPower3-1.0.jar
JAVA_FILES=$(shell find src/ -type f -name '*.java')
CLASS_FILES=$(shell find src/ -type f -name '*.class')
LIBS=$(wildcard lib/*.jar)
LIBS_CP=$(shell echo $(LIBS) | sed -e 's/ /:/g')

all: $(PROG)

$(PROG): $(CLASS_FILES)
	cd src && jar -cvfe ../$@ it.sssup.jsmartpower3.Main $(subst $,\$,$(patsubst src/%,%,$(CLASS_FILES)))

%.class: %.java $(LIBS)
	javac -cp $(LIBS_CP):src $<

run:
	java -cp $(LIBS_CP) -jar $(PROG)

clean:
	rm -f $(CLASS_FILES) $(PROG)
