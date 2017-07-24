package com.quovantis.assignment.themedesigner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    EditText editEmail, editPassword;
    Button buttonLogin;
    OkHttpClient client;
    MediaType JSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        client = new OkHttpClient();
        JSON = MediaType.parse("application/json; charset=utf-8");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String storedToken = pref.getString("token", null);
        if(storedToken != null)
        {
            jumpToListing();
        }

    }

    private void jumpToListing() {
        Intent intent = new Intent(this, ListingActivity.class);
        startActivity(intent);
    }

    public void doLogin(View view) {

        String EMAIL_REGEX = "^[\\w*_\\.+]*[\\w*_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        String PASS_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        Boolean emailIsValid = email.matches(EMAIL_REGEX);
        Boolean passIsValid = password.matches(PASS_REGEX);
        String error = "";
        if(!emailIsValid)
            error+= "Incorrect Email Format.";
        if(!passIsValid)
            error+= "\nIncorrect Password Format.";
        if(!error.equals(""))
            Snackbar.make(view, error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        else
        {
            String urls[] = new String[2];
            urls[0] = email;
            urls[1] = password;
            /*GetTask task = new GetTask();
            task.execute();*/
            PostTask task = new PostTask();
            task.execute(urls);

        }

    }
    public void makeGetRequest(View v) throws IOException {
        GetTask task = new GetTask();
        task.execute();
    }

    public class GetTask extends AsyncTask<String,String,String> {
        private Exception exception;


        protected String doInBackground(String... urls) {
            try {
                String getResponse = get("https://publicobject.com/helloworld.txt");
                return getResponse;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
            Toast.makeText(MainActivity.this, getResponse, Toast.LENGTH_SHORT).show();
        }

        public String get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }


    }

    public void makePostRequest(View v) throws IOException {
        PostTask task = new PostTask();
        task.execute();
    }

    public class PostTask extends AsyncTask<String,String,String> {
        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                String getResponse = post("https://reqres.in/api/login", loginJSON(urls[0], urls[1]));
                return getResponse;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
            //System.out.println(getResponse);
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            String token = "";
            try {
                JSONObject obj = new JSONObject(getResponse);
                token = obj.getString("token");
            } catch (JSONException e) {
                token = null;
                e.printStackTrace();
            }
            editor.putString("token", token);
            editor.commit(); // commit changes
            if(token != null)
                jumpToListing();
            Toast.makeText(MainActivity.this, getResponse, Toast.LENGTH_SHORT).show();
        }

        private String post(String url, String json) throws IOException {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }
    public String loginJSON(String email, String password) {

        String json = "{\"email\":\"" +
                email +
                "\",\"password\":\"" +
                password +
                "\"}";
        return json;
    }
}
