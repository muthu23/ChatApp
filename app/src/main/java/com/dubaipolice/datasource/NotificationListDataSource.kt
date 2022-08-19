package com.dubaipolice.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.model.Notifications
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils


class NotificationListDataSource(
    private val api: ApiRequests,
    private val groupId: String?,
) : PagingSource<Int, Notifications>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notifications> {
        return try {
            val currentLoadingPageKey = params.key ?: 1
            val response = api.getNotificationList(
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!,
                currentLoadingPageKey,
                10
            )
            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey
            LoadResult.Page(
                data = response.data,
                prevKey = prevKey,
                nextKey = if (response.page_count == currentLoadingPageKey) null else currentLoadingPageKey + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Notifications>): Int? {
        return null
    }

}