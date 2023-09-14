# Events SDK Java

This SDK is for general Java applications. However, for Android, please see [the android specific SDK](https://github.com/ht-sdks/events-sdk-android).

## Installation

This SDK is available through [JitPack](https://jitpack.io/#ht-sdks/events-sdk-java/).

### Option A - Maven

Add to `pom.xml`:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

```xml
<dependency>
  <groupId>com.github.ht-sdks.events-sdk-java</groupId>
  <artifactId>analytics</artifactId>
  <version>LATEST</version>
</dependency>
```

### Option B - Gradle:

1. Add JitPack to your build

```gradle
  allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

2. Add your dependendcy

```gradle
compile 'com.github.ht-sdks.events-sdk-java:analytics:+'
```

## Usage

Create an instance of the Analytics object:

```java
import com.hightouch.analytics.Analytics;

public class Main {
  public static void main(String... args) throws Exception {
    final Analytics analytics =
        Analytics.builder(WRITE_KEY)
            .endpoint(API_ENDPOINT)
            .build();
  }
}
```

### Track

```java
analytics.enqueue(TrackMessage.builder("Item Purchased")
    .userId("f4ca124298")
    .properties(ImmutableMap.builder()
        .put("revenue", 39.95)
        .put("shipping", "2-day")
        .build()
    )
);
```

### Screen

```java
analytics.enqueue(ScreenMessage.builder("Schedule")
    .userId("f4ca124298")
    .properties(ImmutableMap.builder()
        .put("category", "Sports")
        .put("path", "/sports/schedule")
        .build()
    )
);
```

### Page

```java
analytics.enqueue(PageMessage.builder("Schedule")
    .userId("f4ca124298")
    .properties(ImmutableMap.builder()
        .put("category", "Sports")
        .put("path", "/sports/schedule")
        .build()
    )
);
```

### Identify

```java
Map<String, String> map = new HashMap();
map.put("name", "Michael Bolton");
map.put("email", "mbolton@example.com");

analytics.enqueue(IdentifyMessage.builder()
        .userId("f4ca124298")
        .traits(map));
```

### Group

```java
analytics.enqueue(GroupMessage.builder("some-group-id")
    .userId("f4ca124298")
    .traits(ImmutableMap.builder()
        .put("name", "Segment")
        .put("size", 50)
        .build()
    )
);
```

### Alias

```java
analytics.enqueue(AliasMessage.builder("previousId")
    .userId("f4ca124298")
);
```

### Context

```java
analytics.enqueue(TrackMessage.builder("Button Clicked")
    .userId("f4ca124298")
    .context(ImmutableMap.builder()
        .put("ip", "12.212.12.49")
        .put("language", "en-us")
        .build()
    )
);
```
