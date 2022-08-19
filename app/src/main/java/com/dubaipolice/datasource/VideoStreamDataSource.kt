package com.dubaipolice.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.model.VideoItem
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils


class VideoStreamDataSource(
    private val api: ApiRequests,
    private val group_id: String?,
) : PagingSource<Int, VideoItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoItem> {
        return try {
            val currentLoadingPageKey = params.key ?: 1
            val response = api.getVideoStreamPaging(
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!,
                group_id,
                currentLoadingPageKey,
                10
            )
            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey
            LoadResult.Page(
                data = response.data.ipstreams,
                prevKey = prevKey,
                nextKey = if (response.page_count == currentLoadingPageKey) null else currentLoadingPageKey + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, VideoItem>): Int? {
        return null
    }

}