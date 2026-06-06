package paulokat.de.docspellviewer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DocspellApi {
    @POST("open/auth/login")
    suspend fun login(@retrofit2.http.Body request: RequestBody): LoginResponse

    @POST("sec/auth/session")
    suspend fun refreshSession(): LoginResponse

    @GET("sec/item/search")
    suspend fun searchItems(
        @Query("q") query: String?,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("withDetails") withDetails: Boolean = true
    ): SearchResponse

    @POST("sec/item/search")
    suspend fun searchItemsPost(@retrofit2.http.Body request: SearchRequestBody): SearchResponse

    @GET("sec/item/searchStats")
    suspend fun searchStats(
        @Query("q") query: String? = null,
        @Query("searchMode") searchMode: String = "normal"
    ): SearchStatsResponse

    @POST("sec/item/searchStats")
    suspend fun searchStatsPost(@retrofit2.http.Body request: SearchRequestBody): SearchStatsResponse

    @GET("sec/item/{id}")
    suspend fun getItem(@Path("id") itemId: String): ItemDetail

    @GET("sec/tag")
    suspend fun listTags(
        @Query("sort") sort: String = "name"
    ): TagListResponse

    @GET("sec/organization")
    suspend fun listOrganizations(
        @Query("sort") sort: String = "name"
    ): ReferenceListResponse

    @GET("sec/person")
    suspend fun listPersons(
        @Query("sort") sort: String = "name"
    ): ReferenceListResponse

    @GET("sec/customfield")
    suspend fun listCustomFields(): CustomFieldListResponse
}

object DocspellApiFactory {
    fun create(baseUrl: String, tokenStore: TokenStore): DocspellApi {
        val authInterceptor = Interceptor { chain ->
            val token = tokenStore.getToken()
            val request: Request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request()
                    .newBuilder()
                    .header("X-Docspell-Auth", token)
                    .build()
            }
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DocspellApi::class.java)
    }
}

data class SearchRequestBody(
    val offset: Int = 0,
    val limit: Int = 20,
    val withDetails: Boolean = true,
    val searchMode: String = "normal",
    val query: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val validMs: Long?
)

data class SearchResponse(
    val groups: List<ItemGroup> = emptyList()
)

data class SearchStatsResponse(
    val count: Int = 0
)

data class ItemGroup(
    val name: String,
    val items: List<ItemSummary> = emptyList()
)

data class NamedRef(
    val id: String? = null,
    val name: String? = null
)

data class AttachmentSummary(
    val id: String,
    val name: String? = null,
    val position: Int? = null
)

data class ItemSummary(
    val id: String,
    val name: String,
    val date: Long? = null,
    val dueDate: Long? = null,
    val source: String? = null,
    val direction: String? = null,
    val corrOrg: NamedRef? = null,
    val corrPerson: NamedRef? = null,
    val concPerson: NamedRef? = null,
    val concEquipment: NamedRef? = null,
    val folder: NamedRef? = null,
    val attachments: List<AttachmentSummary> = emptyList()
)

fun ItemSummary.toDocumentRow(apiBaseUrl: String): DocumentRow {
    val correspondent = formatCorrespondent(corrOrg?.name, corrPerson?.name)
    val primaryAttachment = attachments
        .sortedBy { it.position ?: Int.MAX_VALUE }
        .firstOrNull()
    val attachmentId = primaryAttachment?.id
    val downloadName = primaryAttachment?.name?.takeIf { it.isNotBlank() } ?: name

    return DocumentRow(
        id = id,
        name = name.displayText(),
        correspondent = correspondent.displayText(),
        corrOrgName = corrOrg?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        corrPersonName = corrPerson?.name?.trim()?.takeIf { it.isNotEmpty() }?.displayText(),
        previewUrl = DocspellUrls.itemPreview(apiBaseUrl, id),
        attachmentId = attachmentId,
        viewUrl = attachmentId?.let { DocspellUrls.attachmentView(apiBaseUrl, it) },
        downloadUrl = attachmentId?.let { DocspellUrls.attachmentDownload(apiBaseUrl, it) },
        downloadFileName = downloadName,
        attachmentCount = attachments.size
    )
}

private fun formatCorrespondent(orgName: String?, personName: String?): String {
    val org = orgName?.trim().orEmpty().displayText()
    val person = personName?.trim().orEmpty().displayText()
    return when {
        org.isNotEmpty() && person.isNotEmpty() -> "$org / $person"
        org.isNotEmpty() -> org
        person.isNotEmpty() -> person
        else -> "—"
    }
}

data class ItemDetail(
    val id: String,
    val name: String,
    val direction: String? = null,
    val source: String? = null,
    val state: String? = null,
    val created: Long? = null,
    val updated: Long? = null,
    val itemDate: Long? = null,
    val dueDate: Long? = null,
    val notes: String? = null,
    val corrOrg: NamedRef? = null,
    val corrPerson: NamedRef? = null,
    val concPerson: NamedRef? = null,
    val concEquipment: NamedRef? = null,
    val folder: NamedRef? = null,
    val attachments: List<AttachmentSummary> = emptyList(),
    val tags: List<ItemDetailTag> = emptyList(),
    val customfields: List<ItemCustomField> = emptyList()
)

data class ItemDetailTag(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null
)

data class ItemCustomField(
    val id: String? = null,
    val name: String? = null,
    val label: String? = null,
    val ftype: String? = null,
    val value: String? = null
)

data class CustomFieldListResponse(
    val items: List<CustomFieldDefinition> = emptyList()
)

data class CustomFieldDefinition(
    val id: String,
    val name: String,
    val label: String? = null,
    val ftype: String? = null
)

data class DetailFieldRow(
    val label: String,
    val value: String
)

data class TagListResponse(
    val count: Int = 0,
    val items: List<TagItem> = emptyList()
)

data class TagItem(
    val id: String,
    val name: String,
    val category: String? = null,
    val created: Long? = null
)

data class ReferenceListResponse(
    val items: List<ReferenceItem> = emptyList()
)

data class ReferenceItem(
    val id: String,
    val name: String
)
