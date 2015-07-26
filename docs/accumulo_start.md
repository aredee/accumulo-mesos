# Accumulo Start

The accumulo distribution contains a module called accumulo-start. When executed, this jar
examines the java classpath for any class implementing the KeywordExecutable interface. It
uses the first argument on the class path as an executable keyword.

## Master example

To launch the accumulo master process

`java -jar accumulo-start.jar master` 

Of course nothing is that easy, one must make sure environment variables are set and any 
arguments the master process requires are supplied to the accumulo-start command.

