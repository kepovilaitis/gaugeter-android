package com.example.kestutis.cargauges.helpers;

import com.example.kestutis.cargauges.holders.UserInfoHolder;
import com.google.gson.*;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Field;

@NoArgsConstructor
public class HttpHelper {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient _client = new OkHttpClient();

    public UserInfoHolder loginRequest(String userId, String password) throws IOException {
        JsonObject json = new JsonObject();

        json.addProperty("Username", userId);
        json.addProperty("Password", password);

        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url("https://kepo.lt/api/users/authenticate")
                .header("Content-type", "")
                .header("api-version", "1.0")
                .header("content-encoding", "")
                .post(body)
                .build();

        try (Response response = _client.newCall(request).execute()) {
            GsonBuilder gsonBuilder = new GsonBuilder()
                    .setFieldNamingStrategy(new FieldNamingStrategy() {
                        @Override
                        public String translateName(Field field) {
                            return field.getName().replaceFirst("^(_(is)?)", "").substring(0, 1).toLowerCase() + field.getName().replaceFirst("^(_(is)?)", "").substring(1);
                        }
                    });
            UserInfoHolder user = gsonBuilder.create().fromJson(response.body().string(), UserInfoHolder.class);

            return user;
        }
    }
}
