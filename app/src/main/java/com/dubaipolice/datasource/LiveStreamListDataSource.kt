package com.dubaipolice.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.model.Attachment
import com.dubaipolice.model.Stream
import com.dubaipolice.model.Users
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils


class LiveStreamListDataSource(
    private val api: ApiRequests,
    private val groupId: String?,
) : PagingSource<Int, Stream>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stream> {
        return try {
            val currentLoadingPageKey = params.key ?: 1
            val response = api.getLiveStreamList(
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!,
                groupId,
                currentLoadingPageKey,
                10
            )
            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey
            LoadResult.Page(
                data = response.data.streamList,
                prevKey = prevKey,
                nextKey = if (response.page_count == currentLoadingPageKey) null else currentLoadingPageKey + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Stream>): Int? {
        return null
    }

}