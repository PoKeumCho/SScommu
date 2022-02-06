package com.sscommu.pokeumcho;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class JSONSerializer {

    // 파일명을 정의한다.
    private static final String USER_FILE = "User.json";

    // Context object is necessary to write data to a file
    private Context mContext;

    /** Constructor */
    public JSONSerializer(Context context) {
        mContext = context;
    }

    public void saveUser(User user)
        throws IOException, JSONException {

        // write it to the private disk space of our app
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(USER_FILE, mContext.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(user.convertToJSON().toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public User loadUser()
            throws IOException, JSONException {

        User user = null;
        InputStream in = null;
        BufferedReader reader = null;
        String line = null;

        // 파일이 존재하지 않으면 null 반환
        try { in = mContext.openFileInput(USER_FILE); }
        catch (FileNotFoundException exc) { return null; }

        reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder jsonString = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        // Parsing String with JSONTokener to JSONObject
        JSONTokener tokener = new JSONTokener(jsonString.toString().trim());
        user = new User(new JSONObject(tokener));

        if (reader != null) { reader.close(); }

        return user;
    }
}
