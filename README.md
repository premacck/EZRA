# EZRA
EZ Retrofit Android

Inspired from [a medium article](https://proandroiddev.com/create-retrofit-calladapter-for-coroutines-to-handle-response-as-states-c102440de37a) by [Ahmad El-Melegy](https://melegy.medium.com/)

Ezra makes performing network calls easier, using liveData & coroutines, with following functionalities:
- Reduced boilerplate code for making a network call
- Reduced effort for setting up a project with 
- Setting data in your own `MutableLiveData`, or getting a new `LiveData` on every execution

## Integration

# _ezra_version:_ [![](https://jitpack.io/v/premacck/EZRA.svg)](https://jitpack.io/#premacck/EZRA)


### Gradle

- Add the following in your root `build.gradle` at the end of repositories:
```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

- In your module-level `build.gradle`:
```gradle
implementation "com.github.premacck:EZRA:$ezra_version"
```


### Maven

- Add the JitPack repository to your build file:
```XML
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

- Add the dependency
```XML
<dependency>
  <groupId>com.github.premacck</groupId>
  <artifactId>EZRA</artifactId>
  <version>ezra_version</version>
</dependency>
```

## Usage

#### A common MVVM example:

- Add the `ApiResponseAdapterFactory` to your `Retrofit.Builder` 
```kotlin
Retrofit.Builder()
  // ...
  .addConverterFactory(GsonConverterFactory.create(gson))
  .addCallAdapterFactory(ApiResponseAdapterFactory.create())
  // ...
  .build()
```

- In your Retrofit service class:
```kotlin
interface ApiService {
  // For Default `ApiResponse<Data<DATA>, ApiErrorBody>` calls
  @GET("/") suspend fun someNetworkCall(params): ApiCall<SomeNetworkResponse>
  
  // For more customized responses
  @GET("/") suspend fun someCustomizedNetworkCall(params): ApiResponse<ResponseDataClass, ErrorClass>
}
```

- In your repository interface:
```kotlin
interface ApiRepository {

  // Network call returning some generic response
  fun someNetworkCall(params): LiveData<ApiCall<SomeNetworkResponse>>
  
  // Network call returning some customized response
  fun someCustomizedNetworkCall(params): LiveData<ApiResponse<ResponseDataClass, ErrorClass>>
  
  // Network call that sets the value in specified LiveData, instead of returning a new one
  fun someNetworkCallWithLiveData(params, liveData: MutableLiveData<ApiCall<SomeNetworkResponse>>): LiveData<ApiCall<SomeNetworkResponse>>
}
```

- Then implement your repository, and extend `BaseRepositoryImpl` to get more functionality:
```kotlin
class ApiRepositoryImpl(private val service: ApiService) : ApiInterface, BaseRepositoryImpl() {

  // Network call returning a new live data every time with some generic response
  override fun someNetworkCall(params) = makeApiCall { responseOf { service.someNetworkCall(params) } }

  // Network call returning a new live data every time with some customized response
  override fun someCustomizedNetworkCall(params) = makeApiCall {
    responseOf {
      service.someCustomizedNetworkCall(params)
    }.withTransform {
      // Transform your response here before notifying observers
    }
  }

  // Network call setting valie in (and returning) `liveData` passed as argument every time with some generic response
  override fun someNetworkCallWithLiveData(params, liveData: MutableLiveData<ApiCall<SomeNetworkResponse>>) {
    return makeApiCall(liveData) {
      responseOf { service.someNetworkCall(params) }
    }
  }
}
```

- In your `ViewModel` class:
```kotlin
class ApiViewModel(private val repo: ApiRepository) : ViewModel() {

  // Network call returning a new live data every time with some generic response
  fun someNetworkCall(params) = repo.someNetworkCall(params)

  // Network call returning a new live data every time with some customized response
  fun someCustomizedNetworkCall(params) = repo.someCustomizedNetworkCall(params)

  val apiResponseLiveData by lazy { MutableLiveData<ApiCall<SomeNetworkResponse>>() }

  // Network call setting valie in (and returning) `apiResponseLiveData` every time with some generic response
  fun someNetworkCallWithLiveData(params) = repo.someNetworkCallWithLiveData(params, apiResponseLiveData)
}
```

- In your UI class:
```kotlin
class MyActivity : AppCompatActivity() {

  // ...
  val viewModel by viewModels<ApiViewModel>()

  // ...

  private fun init(...) {
    viewModel.someNetworkCall(params).observe(this) { // it: ApiResponse<Data<SomeNetworkResponse, ApiErrorBody>
      // ...
    }
    viewModel.someCustomizedNetworkCall(params).observe(this) { // it: ApiResponse<ResponseDataClass, ErrorClass>
      // ...
    }
    viewModel.apiResponseLiveData.observe(this) { // it: ApiResponse<Data<SomeNetworkResponse, ApiErrorBody>
      // ...
    }
    callApi()
  }

  private fun callApi() {
    // Useful for pagination scenarios,
    // for making same network calls with different params and getting result in same liveData
    viewModel.someNetworkCallWithLiveData(params)
  }
}
```
