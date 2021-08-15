package project.main.api

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers("Accept:text/plain")
    @GET
    fun getURLResponse(@Url url: String): Call<String>

}