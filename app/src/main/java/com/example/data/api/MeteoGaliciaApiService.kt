package com.example.data.api

import com.example.data.model.MeteoGaliciaDailyResponse
import com.example.data.model.MeteoGaliciaHourlyResponse
import com.example.data.model.MeteoGaliciaObservacionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeteoGaliciaApiService {

    @GET("https://servizos.meteogalicia.gal/mgrss/predicion/jsonPredConcellos.action")
    suspend fun getDailyForecast(
        @Query("idConc") idConc: Int
    ): MeteoGaliciaDailyResponse

    @GET("https://servizos.meteogalicia.gal/mgrss/predicion/jsonPredHorariaConcellos.action")
    suspend fun getHourlyForecast(
        @Query("idConc") idConc: Int
    ): MeteoGaliciaHourlyResponse

    @GET("https://servizos.meteogalicia.gal/mgrss/observacion/observacionConcellos.action")
    suspend fun getObservation(
        @Query("idConcello") idConcello: Int
    ): MeteoGaliciaObservacionResponse
}
