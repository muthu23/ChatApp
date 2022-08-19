package com.dubaipolice.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.model.Users
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils


class MembersDataSource(
    private val api: ApiRequests,
    private val groupId: String?,
    private val organization_id: String?,
    private var query: String?
) : PagingSource<Int, Users>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Users> {
        return try {
            val currentLoadingPageKey = params.key ?: 1
            val response = api.getAllMemberPaging(
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!,
                groupId,
                organization_id,
                currentLoadingPageKey,
                query,
                10
            )
            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey
            LoadResult.Page(
                data = if(response.success) response.data.usersList else emptyList(),
                prevKey = prevKey,
                nextKey = if (!response.success || response.data.usersList.isNullOrEmpty() || response.page_count == 1) null else currentLoadingPageKey + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Users>): Int? {
        return null
    }

    fun search(text: String) {
        query = text
    }
}