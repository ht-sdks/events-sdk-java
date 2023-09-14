package com.hightouch.analytics.http;

import com.hightouch.analytics.messages.Batch;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/** REST interface for the Hightouch API. */
public interface HightouchService {
  @POST
  Call<UploadResponse> upload(@Url HttpUrl uploadUrl, @Body Batch batch);
}
