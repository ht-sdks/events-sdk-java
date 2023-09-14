package com.hightouch.analytics.http;

import com.google.auto.value.AutoValue;
import com.hightouch.analytics.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class UploadResponse {
  public abstract boolean success();
}
