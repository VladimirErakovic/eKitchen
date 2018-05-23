package mkitbs.ekitchen.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import mkitbs.ekitchen.app.entities.Configuration;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.entities.UserRole;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;


public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private DatabaseHandler databaseHandler;
    private EditText serverAddressET;
    private EditText usernameET;
    private EditText passwordET;
    private ProgressDialog progressDialog;
    private List<UserRole> userRoleList;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        databaseHandler = new DatabaseHandler(getApplicationContext());

        serverAddressET = (EditText) findViewById(R.id.serverEditText);
        usernameET = (EditText) findViewById(R.id.usernameEditText);
        passwordET = (EditText) findViewById(R.id.passwordEditText);
        Button loginBtn = (Button) findViewById(R.id.loginButton);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serverAddressET.getText().toString().isEmpty() && !usernameET.getText().toString().isEmpty() && !passwordET.getText().toString().isEmpty()) {

                    // poziv servisa za login
                    new PostLoginCredentials().execute();

                } else {
                    Toast.makeText(LoginActivity.this,
                            getResources().getString(R.string.enter_credentials_msg), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Poziv servisa za login
    private class PostLoginCredentials extends AsyncTask<Void,Void,Void> {

        //"http://mkitsql.mk-group.org/ECafe/api/User/Login";
        private String URL_LOGIN = serverAddressET.getText().toString() + getResources().getString(R.string.service_login);
        private String username;
        private String password;
        private JSONObject jsonObject;
        private String response = "";
        private int responseCode = 0;
        private String multipleUsersError = "";
        private String multipleUserRolesError = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();

            userRoleList = new ArrayList<>();

            username = usernameET.getText().toString();
            password = passwordET.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                jsonObject = new JSONObject();
                jsonObject.put("UserName", username);
                jsonObject.put("Password", password);

                URL url = new URL(URL_LOGIN); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(jsonObject.toString());
                wr.flush();
                wr.close();

                responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";
                }

                if (!response.isEmpty()) {
                    if (!response.equals("\"ERROR\"")) {
                        try {
                            JSONObject jsonObjUser = new JSONObject(response);
                            if (!jsonObjUser.isNull("User")) {
                                JSONArray users = jsonObjUser.getJSONArray("User");

                                if (users.length() > 1)
                                    multipleUsersError = getResources().getString(R.string.multiple_user_error);

                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject userObj = (JSONObject) users.get(i);
                                    String userPicture = "";
                                    if (!userObj.isNull("Picture")) {
                                        userPicture = userObj.getString("Picture");
                                    }
                                    user = new User(userObj.getInt("UserId"),
                                            userObj.getString("UserName"), userObj.getString("Password"),
                                            userObj.getString("Name"), userPicture,
                                            userObj.getInt("CompanyId"), userObj.getInt("RoomId"));
                                    databaseHandler.addUser(user);

                                    if (!userObj.isNull("UserRoles")) {
                                        JSONArray userRoles = userObj.getJSONArray("UserRoles");

                                        if (userRoles.length() > 1)
                                            multipleUserRolesError = getResources().getString(R.string.multiple_user_roles_error);

                                        for (int x = 0; x < userRoles.length(); x++) {
                                            JSONObject userRoleObj = (JSONObject) userRoles.get(x);
                                            UserRole userRole = new UserRole(userRoleObj.getInt("UserId"),
                                                    userRoleObj.getInt("RoleId"));
                                            userRoleList.add(userRole);
                                            databaseHandler.addUserRole(userRole);
                                        }
                                    }
                                }
                                databaseHandler.close();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (!response.equals("\"ERROR\"")) {

                if (!multipleUsersError.isEmpty())
                    Toast.makeText(LoginActivity.this, multipleUsersError, Toast.LENGTH_LONG).show();

                if (!multipleUserRolesError.isEmpty())
                    Toast.makeText(LoginActivity.this, multipleUserRolesError, Toast.LENGTH_LONG).show();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("server_ip_address", serverAddressET.getText().toString());
                    editor.putString("username", usernameET.getText().toString());
                    editor.putString("password", passwordET.getText().toString());
                    editor.apply();

                    new GetConfigurationTableData().execute();

                } else {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,
                            getResources().getString(R.string.service_login_error), Toast.LENGTH_LONG).show();
                }
            } else {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,
                        getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            }
        }
    }


    private class GetConfigurationTableData extends AsyncTask<Void, Void, Void> {

        private String serverAddress;
        private String serverUriReplaced;
        private String multipleConfigurationsError = "";
        private Configuration configuration;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            serverAddress = serverAddressET.getText().toString();
            serverUriReplaced = serverAddress;
            serverUriReplaced = serverUriReplaced.replace("http://", "");
            serverUriReplaced = serverUriReplaced.replace(".", "&");

            Log.d("SERVER_URI", serverUriReplaced);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // "http://mkitsql.mk-group.org/ECafe/api/Configuration/ViewConfiguration/" + "mkitsql&mk-group&org"
            String URL_USER = serverAddress + getResources().getString(R.string.view_configuration) + serverUriReplaced;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_USER);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            String jsonConfiguration = result.toString();

            if (!jsonConfiguration.isEmpty()) {
                try {
                    JSONObject jsonObjConf = new JSONObject(jsonConfiguration);
                    if (!jsonObjConf.isNull("Configuration")) {
                        JSONArray configurations = jsonObjConf.getJSONArray("Configuration");

                        if (configurations.length() > 1)
                            multipleConfigurationsError = getResources().getString(R.string.multiple_configurations_error);

                        for (int i = 0; i < configurations.length(); i++) {
                            JSONObject confObj = (JSONObject) configurations.get(i);

                            Date installTime = null;
                            installTime = new DateTime(confObj.getString("InstallationTime")).toDate();

                            configuration = new Configuration(confObj.getInt("ConfigurationId"), confObj.getString("ServerIpAddress"),
                                    confObj.getString("ApplicationVersion"), installTime, confObj.getInt("KitchenRoleId"),
                                    confObj.getInt("HallRoleId"), confObj.getInt("WaiterRoleId"), confObj.getInt("CustomerRoleId"));
                            databaseHandler.addConfiguration(configuration);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Configuration Data", "Didn't receive any data from server!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (!multipleConfigurationsError.isEmpty())
                Toast.makeText(LoginActivity.this, multipleConfigurationsError, Toast.LENGTH_LONG).show();


            UserRole userRole = userRoleList.get(0);

            String userKind = "";
            String error = "";
            if (userRole.getUserId() != user.getUserId())
                error = getResources().getString(R.string.role_id_error);

            if (userRole.getRoleId() == configuration.getCustomerRoleId()) {
                userKind = "CUSTOMER";
            } else if (userRole.getRoleId() == configuration.getWaiterRoleId()) {
                userKind = "WAITER";
            } else if (userRole.getRoleId() == configuration.getHallRoleId()) {
                userKind = "MEETING_ROOM";
            } else if (userRole.getRoleId() == configuration.getKitchenRoleId()) {
                userKind = "KITCHEN";
            } else {
                error = getResources().getString(R.string.unknown_role_error);
            }

            if (error.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("user_kind", userKind).apply();
                Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this,
                    error + ": " + getResources().getString(R.string.error_call_admin), Toast.LENGTH_LONG).show();
            }
        }
    }

}
