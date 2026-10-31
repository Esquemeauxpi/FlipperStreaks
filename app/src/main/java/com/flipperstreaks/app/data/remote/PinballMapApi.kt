package com.flipperstreaks.app.data.remote

import com.flipperstreaks.app.data.remote.dto.AuthDetailsResponse
import com.flipperstreaks.app.data.remote.dto.GenericApiResponse
import com.flipperstreaks.app.data.remote.dto.LocationMachinesResponse
import com.flipperstreaks.app.data.remote.dto.LocationsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Thin wrapper over https://pinballmap.com/api/v1 — see /api/v1/docs.
// Read endpoints work unauthenticated. Write endpoints pick up
// user_email/user_token automatically via AuthInterceptor when a
// user has connected their account; otherwise they'll 401 and the
// caller should surface WriteResult.RequiresAuth (see domain/usecase).
interface PinballMapApi {

    // Returns a real LIST of locations within a lat/lon box — this is the
    // correct endpoint for a "nearby" browse screen. (closest_by_lat_lon.json
    // returns only the single nearest location unless send_all_within_distance
    // is set, and even then isn't wrapped the same way — don't use it here.)
    @GET("locations/within_bounding_box.json")
    suspend fun getNearbyLocations(
        @Query("swlat") swLat: Double,
        @Query("swlon") swLon: Double,
        @Query("nelat") neLat: Double,
        @Query("nelon") neLon: Double
    ): LocationsResponse

    @GET("locations/{id}/machine_details.json")
    suspend fun getMachinesForLocation(@Path("id") locationId: Int): LocationMachinesResponse

    @GET("locations.json")
    suspend fun searchLocations(@Query("by_city_or_name") query: String): LocationsResponse

    @GET("users/auth_details.json")
    suspend fun getAuthDetails(
        @Query("user_email") email: String,
        @Query("user_password") password: String
    ): AuthDetailsResponse

    @FormUrlEncoded
    @POST("locations/{id}/machine_conditions.json")
    suspend fun submitMachineComment(
        @Path("id") locationId: Int,
        @Field("machine_id") machineId: Int,
        @Field("comment") comment: String
    ): GenericApiResponse

    @FormUrlEncoded
    @POST("locations/{id}/add_machine.json")
    suspend fun addMachineToLocation(
        @Path("id") locationId: Int,
        @Field("machine_id") machineId: Int
    ): GenericApiResponse

    @FormUrlEncoded
    @POST("location_machine_xrefs/{id}/high_score.json")
    suspend fun submitHighScore(
        @Path("id") xrefId: Int,
        @Field("score") score: Long,
        @Field("initials") initials: String
    ): GenericApiResponse
}
