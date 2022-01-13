Glass
=====

### Opus #9

This miniature framework is designed to make working with blind, version-dependent method names much easier.
It allows the user to link a single template interface to a nondescript object at runtime and call its methods directly, without needing reflection or multiple per-version classes.


## Maven Information
```xml
<repository>
    <id>kenzie</id>
    <name>Kenzie's Repository</name>
    <url>https://repo.kenzie.mx/releases</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>glass</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

## Introduction

This utility was designed for Minecraft server development to make working with the proprietary server code ("NMS") easier, as method and class names are often changed between major and minor versions.
The two existing methods of using NMS are to:
- create an edition of each dependent class for each version, using the methods directly (low overhead, high effort)
- use reflection to access NMS methods by name (high overhead, low effort)

Glass aims to solve this problem by giving a low overhead, low effort alternative: a single interface can be mapped to multiple NMS versions, with the linking code written at runtime by the library, when you know which version of the class to use.
It functions similarly to a proxy but with the advantage of smarter mapping and no methodhandles.

Glass requires no reflective method calls to the actual object.

## Example

```java
public interface Thing
    extends Window {

    @Target(handler = "1_16", name = "getVersion")
    @Target(handler = "1_17", name = "v")
    int getVersion();

}
```

This example is designed for linking to an internal "version" method, for which the name changes between versions.
You could then use it as follows:
```java 
final Thing window = glass.createWindow(Thing.class, server, "1_17");
assert window.getVersion() == ...;
```

## Explanation

When a window is mapped, the framework writes a special internal class that implements the template interface, and bridges each method to the one available at runtime.

The `handler` parameter is used as a way of differentiating between these, and a Target with no `handler` will be used as the default.

Glass is able to intelligently convert compatible parameter types (e.g. Object -> String) in case the actual method parameter or return type is unavailable at the program's compile-time, or otherwise unusable.

Mapped window classes will be cached and reused where possible, and disposed of once the creating `Glass` instance and all uses have been garbage-collected for memory efficiency.
