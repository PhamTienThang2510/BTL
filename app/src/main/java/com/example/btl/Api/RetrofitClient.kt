package com.example.btl.Api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Backend runs on port 3000 (configured in main.ts with process.env.PORT ?? 3000)
    // No global /api prefix - endpoints start directly from /auth, /products, etc.
    // Swagger available at: /api-docs (but app doesn't use this)

    // Choose based on your setup:
    // - Android Emulator: http://10.0.2.2:3000 (10.0.2.2 = host machine localhost)
    // - Same Wi-Fi device: http://<LAN-IP>:3000 (e.g., 192.168.1.100:3000)
    // - Local machine: http://localhost:3000

    private const val BASE_URL_EMULATOR = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net/"
    private const val BASE_URL_LAN = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net/"
    private const val BASE_URL_LOCAL = "https://giang-backend-fdfkdfb4fycff2hs.malaysiawest-01.azurewebsites.net/"

    // CONFIGURE HERE: Choose which environment you're testing with
    private const val BASE_URL = BASE_URL_EMULATOR  // Change to BASE_URL_LAN or BASE_URL_LOCAL as needed

    private var context: Context? = null

    fun init(appContext: Context) {
        context = appContext
    }

    private fun getOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()

        if (context != null) {
            httpClient.addInterceptor(AuthInterceptor(context!!))
        }

        return httpClient.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val categoryApi: CategoryApi by lazy {
        retrofit.create(CategoryApi::class.java)
    }

    val productApi: ProductApi by lazy {
        retrofit.create(ProductApi::class.java)
    }

    val cartApi: CartApi by lazy {
        retrofit.create(CartApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val addressApi: AddressApi by lazy {
        retrofit.create(AddressApi::class.java)
    }

    val orderApi: OrderApi by lazy {
        retrofit.create(OrderApi::class.java)
    }

    val mockupApi: MockupApi by lazy {
        retrofit.create(MockupApi::class.java)
    }

    val locationApi: LocationApi by lazy {
        retrofit.create(LocationApi::class.java)
    }

    val reviewApi: ReviewApi by lazy {
        retrofit.create(ReviewApi::class.java)
    }

    val refundApi: RefundApi by lazy {
        retrofit.create(RefundApi::class.java)
    }

    // Public method to get Retrofit instance for creating custom API services
    fun getRetrofitInstance(): Retrofit {
        return retrofit
    }
}
