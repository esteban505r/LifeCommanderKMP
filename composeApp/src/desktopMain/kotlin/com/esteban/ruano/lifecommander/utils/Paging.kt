import androidx.paging.*
import kotlinx.coroutines.flow.Flow

// ===================== Generic PagingSource & Pager helpers =====================

/**
 * Generic offset-based PagingSource.
 * - Key = offset (Int)
 * - Value = T
 * - Fetcher must return a list of size <= limit. When < limit, we stop paging.
 */
class OffsetPagingSource<T : Any>(
    private val pageSizeHint: Int,
    private val fetchPage: suspend (limit: Int, offset: Int) -> List<T>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> = try {
        val offset = params.key ?: 0
        val limit = params.loadSize.coerceAtLeast(1)

        val data = fetchPage(limit, offset)

        val prev = if (offset == 0) null else (offset - limit).coerceAtLeast(0)
        val next = if (data.size < limit) null else offset + data.size

        LoadResult.Page(
            data = data,
            prevKey = prev,
            nextKey = next
        )
    } catch (t: Throwable) {
        LoadResult.Error(t)
    }

    /**
     * Current Paging guidance:
     * derive refresh key from anchorPosition and closest page; compute a nearby offset using pageSize.
     * This keeps the user near the same spot after invalidation.
     */
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor)
        val size = state.config.pageSize.takeIf { it > 0 } ?: pageSizeHint
        return page?.prevKey?.let { it + size } ?: page?.nextKey?.let { (it - size).coerceAtLeast(0) }
    }
}

fun <T:Any> offsetPager(
    pageSize: Int = 20,
    fetchPage: suspend (limit: Int, offset: Int) -> List<T>
): Flow<PagingData<T>> =
    Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize / 2,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { OffsetPagingSource(pageSize, fetchPage) }
    ).flow