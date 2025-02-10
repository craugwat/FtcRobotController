package org.firstinspires.ftc.teamcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.RobotLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LimeLightImageTools {
    Limelight3A limeLight;



    LimeLightImageTools(Limelight3A limeLight) {
        this.limeLight = limeLight;
    }

    public boolean SendNewSnapshotToDashboard() {
        String snapShotName = "snapshot";
        FtcDashboard dashboard = FtcDashboard.getInstance();

        boolean captured = limeLight.captureSnapshot(snapShotName);

        JSONObject obj = snapshotManifest();
        String snapShotFullName ="";

        if (obj != null) {
            try {
                snapShotFullName = findFullName(obj, snapShotName);
            } catch (JSONException e) {
                return false;
            }
            if (snapShotFullName != "") {
                Bitmap snapShot = getBitmapFromSnapShot(snapShotFullName);
                if (snapShot != null) {
                    dashboard.sendImage(snapShot);
                    limeLight.deleteSnapshots();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * find the first occurrence  of a string name in a JSONObject-JSONArray
     *
     * @param fileName full name of file we want to get from limelight snapshot directory
     * @return A Bitmap image if we get it from limelight, or return null
     */
    public Bitmap getBitmapFromSnapShot(String fileName) {
        String imageUrl = "http://172.29.0.1:5801/snapshots/" + fileName; // Replace with your image URL
//        String imageUrl = "http://172.29.0.1:5800";
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            RobotLog.d("LLIT getBitmapFromSnapShot IOException" );
            return null;
        }
    }


    /**
     * find the first occurrence  of a string name in a JSONObject-JSONArray
     *
     * @param obj source to search through
     * @param snapShotName begining of name to search for
     * @return A Sting of the full name, limelight adds some characters to names we provide
     */
    public String findFullName(JSONObject obj, String snapShotName) throws JSONException {
        try {
            JSONArray jsonArray = obj.getJSONArray("fileNames");
            for (int i = 0; i < jsonArray.length(); i++) {
                String str = jsonArray.getString(i);
                if (str.contains(snapShotName)) {
                    return str;
                }
            }
        } catch (JSONException e) {
            return "";
        }
        return "";
    }
    /**
     * Sends a GET request to the specified endpoint.
     *
     * @param endpoint The endpoint to send the request to.
     * @return A JSONObject containing the response, or null if the request fails.
     */
    public JSONObject sendGetRequest (String endpoint) {

        // todo  fix this baseUrl (debug and see what it is in the Limelight3A.java
        String baseUrl = "http://172.29.0.1:5807";
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

        HttpURLConnection connection = null;
        try {
            String urlString = baseUrl + endpoint;
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(GETREQUEST_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                if (isValidJson(response)) {
                    return new JSONObject(response);
                } else{
                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray(response);
                    jsonObject.put("fileNames", jsonArray);
                    return jsonObject;
                }
            } else {
                System.out.println("HTTP GET Error: " + responseCode);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * Gets the manifest of available snapshots.
     * This method is not necessary for FTC teams. Marked as private
     *
     * @return A JSONObject containing the snapshot manifest.
     */
    public JSONObject snapshotManifest() {
        return sendGetRequest("/snapshotmanifest");
    }


    /**
     * Reads the response from an HTTP connection.
     *
     * @param connection The HttpURLConnection to read from.
     * @return A String containing the response.
     * @throws IOException If an I/O error occurs.
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    public static boolean isValidJson(String jsonString) {
        try {
            new JSONObject(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void del (Limelight3A limelight) {

        limelight.deleteSnapshots();
        RobotLog.d("LLIT deleted all snapshots" );

        String snapShotName = "snapshot";
        boolean captured = limelight.captureSnapshot(snapShotName);
        RobotLog.d("LLIT Captured=", captured );
        JSONObject obj = snapshotManifest();
        RobotLog.d("LLIT manifest= ", obj);
        String snapShotFullName;
        try {
            snapShotFullName = findFullName(obj, snapShotName);
        } catch (JSONException e) {
            snapShotFullName = "";
        }

        RobotLog.d("LLIT fullname = ", snapShotFullName);

        boolean deleted1 = limelight.deleteSnapshot(snapShotName);
        RobotLog.d("LLIT deleted short name = ", deleted1);

        obj = snapshotManifest();
        RobotLog.d("LLIT manifest= ", obj);

        deleted1 = limelight.deleteSnapshot(snapShotFullName);
        RobotLog.d("LLIT deleted full name = ", deleted1);

        obj = snapshotManifest();
        RobotLog.d("LLIT manifest= ", obj);
    }

    // this worked and saved a file onto the control hub.
    // I then was able to open the file in a hex editor, search for
    // JPG begin signatures = FFD8FF an jpg trailer = FFD9.
    // i copied the bytes from begin to end signatures and saved in a new file
    // this new file opened as a viewable jpg! So the jpg is in there!
    private String readResponse4(HttpURLConnection connection) throws IOException {
        InputStream is = connection.getInputStream();
        int cnt = 0;
        RobotLog.d("LLIT4 cnt=" + cnt );
        try (OutputStream outputStream = new FileOutputStream("/sdcard/FIRST/CW8620")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
                cnt += bytesRead;
                RobotLog.d("LLIT4a cnt=" + cnt );
            }
        }
        RobotLog.d("LLIT4b cnt=" + cnt );
        return "";
    }

    // This is NOT optimized for streams!!!
    // I'm doing single byte reads and tests from the input streams!
    // BUT IT WORKS!
    enum STATES {start1, start2, start3, body, end1, done};
    public boolean streamToDashboard () {
        // todo  fix this baseUrl (debug and see what it is in the Limelight3A.java
        String baseUrl = "http://172.29.0.1:5800";
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;
        STATES state = STATES.start1;
        boolean retValue = false;

        HttpURLConnection connection = null;
        try {
            String urlString = baseUrl;// + endpoint;
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(GETREQUEST_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                InputStream is = connection.getInputStream();
                int cnt = 0;

                int bufferSize = 200000;
                byte[] buffer = new byte[bufferSize];
                int theByte;
                int index = 0;
                while ( (theByte = is.read()) != -1 && state != STATES.done) {
                    switch (state) {
                        case start1:
                            if (theByte == 0xFF)
                                state = STATES.start2;
                            break;
                        case start2:
                            if (theByte == 0xD8)
                                state = STATES.start3;
                            else
                                state = STATES.start1;
                            break;
                        case start3:
                            if (theByte == 0xFF)
                                state = STATES.body;
                            else
                                state = STATES.start1;
                            break;
                        case body:
                            if (theByte == 0xFF)
                                state = STATES.end1;
                            break;
                        case end1:
                            if (theByte == 0xD9)
                                state = STATES.done;
                            else
                                state = STATES.body;
                            break;
                    }
                    if (state != STATES.start1 && index<bufferSize) {
                        buffer[index++] = (byte)theByte;
                    }
                }

                RobotLog.d("LLIT streamToDashboard done. index= " + index );
                Bitmap bmp = BitmapFactory.decodeByteArray(buffer,0,index);
                FtcDashboard.getInstance().sendImage(bmp);
                retValue = true;

            } else {
                RobotLog.d("LLIT HTTP GET Error =  " + responseCode );
            }
        } catch (Exception e) {
            RobotLog.d("LLIT   Exception=  " + e );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return retValue;
    }


    // ***  Begin attempt for more optimized stream access  ***
    // Does not work once we get to the actual image data, it is converted
    // from bytes to strings and non printable characters are messed up.
    // the do not convert back the same when passed to BitmapFactory
    public  Bitmap decodeMultipartImage(String urlString)  {
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(GETREQUEST_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            InputStream inputStream = connection.getInputStream();
            String contentType = connection.getHeaderField("Content-Type");
            String boundary = extractBoundary(contentType);
            if (boundary == null) {
                RobotLog.d("LLIT boundary is NULL Exception - ");
                return null;
            }
            return readImageFromStream(inputStream, boundary);
        } catch (Exception e) {
            RobotLog.d("LLIT decodeMultipartImage Exception - " + e );
            return null;
        } finally {
            if (connection != null) {
            connection.disconnect();
            }
        }

    }

    private  String extractBoundary(String contentType) {
        Pattern pattern = Pattern.compile("boundary=(.*)");
        Matcher matcher = pattern.matcher(contentType);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Bitmap readImageFromStream(InputStream inputStream, String boundary) {
        String marker = "--" + boundary;
        byte[] buffer = new byte[1000];
        int bytesRead=0;
        StringBuilder data = new StringBuilder();
        Bitmap bmp = null;

        try {
            if (inputStream != null) {
                bytesRead = inputStream.read(buffer);
                data.append(new String(buffer, 0, bytesRead));
                String dataString = data.toString();
                String LengthStringKey = "Content-Length: ";
                int sizeIndexStart = dataString.indexOf(LengthStringKey) + LengthStringKey.length();
                int sizeIndexEnd = dataString.indexOf("\r\n", sizeIndexStart);
                String sizeString = dataString.substring(sizeIndexStart, sizeIndexEnd);
                int size = Integer.parseInt(sizeString);
                int imageStart = dataString.indexOf("\r\n\r\n")+4;
                byte[] byteBuffer = Arrays.copyOfRange(buffer, imageStart, size+1000); // size is used to force new buffer size
                bytesRead -= imageStart; // remove the header bytes and start counting at image start.

                int i = 0;
                while (bytesRead < size && i != -1) {
//                    int numberToRead = Math.min(4096, size-bytesRead);
                    int numberToRead = size-bytesRead;
                    i = inputStream.read(byteBuffer, bytesRead, numberToRead);
                    bytesRead += i;
                }
                if (i==-1) {
                    RobotLog.d("LLIT no more bytes to get" );
                }

                bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(byteBuffer));
                if (bmp==null) {
                    RobotLog.d("LLIT bmp is null" );
                }
                return bmp;

            } else {
                RobotLog.d("LLIT readImageFromStream - input stream null" );
                return bmp;
            }
        } catch (Exception e) {
            RobotLog.d("LLIT readImageFromStream Exception byteRead="+bytesRead+"  e=" + e);
            //e.printStackTrace();
            return null;
        }
    }
    // ***  Begin attempt for more optimized stream access   ***

}
