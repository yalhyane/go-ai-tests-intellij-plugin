package com.yalhyane.intellij.goaitests;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class OkHttpChatGptApi {


    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");
    private String token;

    public OkHttpChatGptApi(String token) {
        this.token = token;
    }

    public String completion(String prompt) throws Exception {

        JSONObject data = new JSONObject();
        data.put("model", "text-davinci-003");
//        data.put("model", "gpt-4");
        data.put("prompt", prompt);
        data.put("max_tokens", 500);
        data.put("temperature", 1);
//        System.out.println("JSON: " + data.toString());
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .addHeader("Authorization", "Bearer " + this.token)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MEDIA_TYPE_JSON, data.toString()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()){
                System.out.println("ChatGPT response: \n" + response.body().string());
                throw new IOException("Unexpected code " + response);
            };
            String body = response.body().string();

            System.out.println("ChatGPT response body: " + body);
            JSONObject o = new JSONObject(body);
            System.out.println("ChatGPT response choices: \n" + o.getJSONArray("choices").toString());


            return o.getJSONArray("choices").getJSONObject(0).getString("text");
        }
    }
}
