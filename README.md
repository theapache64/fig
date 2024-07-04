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
    val fig = Fig()
    fig.init(sheetUrl = "https://docs.google.com/spreadsheets/d/../edit?usp=sharing") // your Google sheet URL
    println("Fruit is '${fig.getValue("fruit", null)}'")
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
    implementation("com.github.theapache64:fig:0.0.3")
}
```

<details>
  <summary>Groovy:</summary>
  
  ```groovy
repositories {
    maven { url 'https://jitpack.io' } // Add jitpack
}
dependencies {
    implementation 'com.github.theapache64:fig:0.0.3'
}
 ```
</details>



4. ⌨️ Use `Fig.init` and `Fig.getValue`

```kotlin
suspend fun main() {
    val fig = Fig()
    fig.init(sheetUrl = "YOUR-GOOGLE-SHEET-URL-GOES-HERE")
    println("Fruit is '${fig.getValue("fruit", null)}'")
}
```


5. 💻 **Output**

```
Fruit is 'apple'
```

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
