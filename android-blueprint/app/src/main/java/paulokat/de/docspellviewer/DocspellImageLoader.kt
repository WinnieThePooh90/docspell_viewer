package paulokat.de.docspellviewer

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

object DocspellImageLoader {
    private const val DISK_CACHE_BYTES = 100L * 1024L * 1024L

    fun create(context: Context, tokenStore: TokenStore): ImageLoader {
        val authClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenStore.getToken()
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request()
                        .newBuilder()
                        .header("X-Docspell-Auth", token)
                        .build()
                }
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .okHttpClient(authClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("docspell_thumbnails"))
                    .maxSizeBytes(DISK_CACHE_BYTES)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    fun invalidateCaches(loader: ImageLoader) {
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}
