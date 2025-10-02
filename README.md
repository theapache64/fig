![](cover.jpeg)


# fig


<a href="https://twitter.com/theapache64" target="_blank">
<img alt="Twitter: theapache64" src="https://img.shields.io/twitter/follow/theapache64.svg?style=social" />
</a>

> Use Google sheet as remote config

### ✨ Demo

**Sheet:**

<img src="https://github.com/theapache64/fig/assets/9678279/fb610e72-f880-4131-b9fd-0f8e255a862e" width="300"/>


**Code:**
```kotlin
suspend fun main() {
    val fig = Fig(sheetUrl = "https://docs.google.com/spreadsheets/d/../edit?usp=sharing") // your Google sheet URL
    fig.load() // call this before calling any getXXX function
    println("Fruit is '${fig.getString("fruit", null)}'")
}
```

**Output:**
```
Fruit is 'apple'
```

## ⌨️ Usage

1. 📄 Create a Google Sheet with two columns `key` and `value`
<img src="https://github.com/theapache64/fig/assets/9678279/fb610e72-f880-4131-b9fd-0f8e255a862e" width="300"/>

2. 🔗 Choose "Anyone with link" and copy the link

<img src="https://github.com/theapache64/fig/assets/9678279/1e789776-aabb-40c5-a7a8-97aca27108b3" width="300"/>



3. 🤝 Add dependency

![latestVersion](https://img.shields.io/github/v/release/theapache64/fig)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // [latest version - i promise!]
    implementation("com.github.theapache64:fig:0.2.2")
}
```

<details>
  <summary>Groovy:</summary>
  
  ```groovy
repositories {
    maven { url 'https://jitpack.io' } // Add jitpack
}
dependencies {
    // [latest version - i promise!]
    implementation 'com.github.theapache64:fig:0.2.2'
}
 ```
</details>



4. ⌨️ Use `Fig.load` and `Fig.getXXX`

```kotlin
suspend fun main() {
    val fig = Fig(sheetUrl = "YOUR-GOOGLE-SHEET-URL-GOES-HERE")
    fig.load() 
    println("Fruit is '${fig.getString("fruit", null)}'")
}
```


5. 💻 **Output**

```
Fruit is 'apple'
```

## ⏱️ Cache Management with TTL (Time To Live)

All `getXXX` methods support an optional `timeToLive` parameter that allows you to control cache freshness. When a TTL is specified, Fig will automatically refresh the cache from the Google Sheet when the specified duration has elapsed.

### Available Methods with TTL Support

All data retrieval methods have suspend versions with TTL support:
- `suspend fun getString(key: String, defaultValue: String? = null, timeToLive: Duration? = null)`
- `suspend fun getInt(key: String, defaultValue: Int? = null, timeToLive: Duration? = null)`
- `suspend fun getLong(key: String, defaultValue: Long? = null, timeToLive: Duration? = null)`
- `suspend fun getFloat(key: String, defaultValue: Float? = null, timeToLive: Duration? = null)`
- `suspend fun getDouble(key: String, defaultValue: Double? = null, timeToLive: Duration? = null)`
- `suspend fun getBoolean(key: String, defaultValue: Boolean? = null, timeToLive: Duration? = null)`

### Usage Example

```kotlin
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val fig = Fig(sheetUrl = "YOUR-GOOGLE-SHEET-URL-GOES-HERE")
    fig.load()
    
    // Cache will be refreshed every 5 minutes for this specific call site
    val apiKey = fig.getString("api_key", null, timeToLive = 5.minutes)
    
    // Cache will be refreshed every 30 seconds for this specific call site
    val isFeatureEnabled = fig.getBoolean("feature_flag", false, timeToLive = 30.seconds)
    
    println("API Key: $apiKey")
    println("Feature Enabled: $isFeatureEnabled")
}
```

### How TTL Works

- **Call-site specific**: Each call site (unique location in your code) maintains its own TTL timer
- **Automatic refresh**: When the TTL expires, Fig automatically fetches fresh data from the Google Sheet
- **No manual intervention**: You don't need to manually check or refresh the cache
- **Coroutine-based**: TTL methods are suspend functions and must be called from a coroutine context

**Example:**
```kotlin
// Call site A - refreshes every 10 seconds
val value1 = fig.getString("key", null, timeToLive = 10.seconds)

// Call site B - refreshes every 1 minute (independent from call site A)
val value2 = fig.getString("key", null, timeToLive = 1.minutes)
```

Even though both calls request the same key, they maintain separate TTL timers based on their call site location in the code.

## ▶️ Video Tutorial
If you want to see this library in practice, you can check out this video tutorial on YouTube: https://youtu.be/E8X94pCJ2zs 

## 🚫 Limitations 
- Your value field can't have two data types. To solve this always wrap your number inputs with `TO_TEXT` function. Eg: `=TO_TEXT("2.4")`
- This library uses an unofficial Google Sheets API to fetch data, which may stop working at any time. It's best to use this library only for small projects where you need quick, dynamic values without setting up something like Firebase (and honestly, most of my projects use this library as a config source and database, so if it crashes, I'll be crying right alongside you).


## ✍️ Author

👤 **theapache64**

* Twitter: <a href="https://twitter.com/theapache64" target="_blank">@theapache64</a>
* Email: theapache64@gmail.com

Feel free to ping me 😉

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

1. Open an issue first to discuss what you would like to change.
1. Fork the Project
1. Create your feature branch (`git checkout -b feature/amazing-feature`)
1. Commit your changes (`git commit -m 'Add some amazing feature'`)
1. Push to the branch (`git push origin feature/amazing-feature`)
1. Open a pull request

Please make sure to update tests as appropriate.

## ❤ Show your support

Give a ⭐️ if this project helped you!

<a href="https://www.patreon.com/theapache64">
  <img alt="Patron Link" src="https://c5.patreon.com/external/logo/become_a_patron_button@2x.png" width="160"/>
</a>

<a href="https://www.buymeacoffee.com/theapache64" target="_blank">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" width="160">
</a>


## 📝 License

```
Copyright © 2024 - theapache64

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

_This README was generated by [readgen](https://github.com/theapache64/readgen)_ ❤
