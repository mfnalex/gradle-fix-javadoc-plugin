This is a gradle plugin to automatically remove doubled annotations caused by, for example, jetbrains annotations.

## What does this do?

Imagine you have the following code:

```java
import org.jetbrains.annotations.NotNull;

/**
 * Example class
 */
public class Example {

    /**
     * Reverses the given string
     * @param input String to reverse
     * @return Reversed string
     */
    @NotNull
    public String reverseString(@NotNull String input) {
        return new StringBuilder(input).reverse().toString();
    }
}
```

If you generate javadocs, the NotNull annotations appear twice in the output:

![img.png](img/broken.png)

Using the plugin, the doubled annotations are removed from the javadoc output:

![img.png](img/fixed.png)

## Usage
Add my repository to your settings.gradle.kts file:
```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.jeff-media.com/public")
        }
    }
}
```
Now you can apply the plugin in your build.gradle.kts file:
```kotlin
plugins {
    id("com.jeff-media.fix-javadoc-plugin") version("1.1")
}
```

The plugin will automatically generate a fix<taskName> for every task that extends Javadoc. The fix<taskName> task will be executed after the respective Javadoc task.