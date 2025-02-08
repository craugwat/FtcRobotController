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
import java.util.Base64;


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

    public int DirectfromURL() throws MalformedURLException {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        String imageUrl = "http://172.29.0.1:5800"; // Replace with your image URL
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return 2;
        }

        if (bitmap != null) {
            dashboard.sendImage(bitmap);
            limeLight.deleteSnapshots();
            return 3;
        }
        return 4;
    }

    public int webCam(){
        return 0;
    }





    /*****************************************/
    /**
     * Sends a GET request to the specified endpoint.
     *
     * @return A JSONObject containing the response, or null if the request fails.
     */
    public JSONObject sendGetRequest2 () {

        // todo  fix this baseUrl (debug and see what it is in the Limelight3A.java
        String baseUrl = "http://172.29.0.1:5800";
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

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
                String response = readResponse5(connection);
                if (isValidJson(response)) {
                    return new JSONObject(response);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray(response);
                    jsonObject.put("fileNames", jsonArray);
                    return jsonObject;
                }
            } else {
                System.out.println("HTTP GET Error: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

        private String readResponse2(HttpURLConnection connection) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;

//            boolean empty = true;
//            for (int i=0;i<8000;i++) {
//                int ch = reader.read();
//                if (ch != 0) {
//                    empty = false;
//                }
//
//            }
            int i=0;
            while ((line = reader.readLine()) != null && i<1000) {
                response.append(line);
                RobotLog.d("LLIT i=" + i +"  line=" + line );
                i++;
//            response.append(empty);
            }
            reader.close();

            return response.toString();

            //            InputStream inputStream = connection.getInputStream();
//            // Read from inputStream
//            int size = inputStream.available();
//            byte[] bytes = new byte[size];
//            int rxCnt = inputStream.read(bytes);
//            reader.close();
//            return " " + bytes[rxCnt];



        }

    private String readResponse3(HttpURLConnection connection) throws IOException {
        InputStream is = connection.getInputStream();
        while (is.available()>0) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) {
//                return null;
            } else {
                return "got a bitmap";
            }
        }
        return null;
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
                outputStream.write(buffer, 0, bytesRead);
                cnt += bytesRead;
                RobotLog.d("LLIT4a cnt=" + cnt );
            }
        }
        RobotLog.d("LLIT4b cnt=" + cnt );
        return "";
    }

    private String readResponse5(HttpURLConnection connection) throws IOException {
        RobotLog.d("LLIT5a");
        InputStream is = connection.getInputStream();
        RobotLog.d("LLIT5b");
        decode(is,"--boundarydonotcross");
        RobotLog.d("LLIT5c");
        return "";
    }


    /*****************************************/
    // multipart-x-mixed-replace
        public void decode(InputStream inputStream, String boundary) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder currentPart = new StringBuilder();
            boolean firstBoundaryFound = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains(boundary)) {
                    if (firstBoundaryFound) {
                        String str =currentPart.toString();
                        processPart(str);
                    } else {
                        firstBoundaryFound = true;
                    }
                    currentPart = new StringBuilder();
                } else {
                    currentPart.append(line).append("\n");
                }
            }
            //Process the last part if the stream ends without a boundary
            if(currentPart.length() > 0) {
                processPart(currentPart.toString());
            }
        }

    private void processPart(String partContent) throws UnsupportedEncodingException {
        String[] parts = partContent.split("\n\n", 2);
        if (parts.length < 2) return;

        String headers = parts[0];
        String body = parts[1];

        // Process headers (e.g., get Content-Type)
        String contentType = "";
        String[] headerLines = headers.split("\n");
        for (String headerLine : headerLines) {
            if (headerLine.startsWith("Content-Type:")) {
                contentType = headerLine.substring("Content-Type:".length()).trim();
                break;
            }
        }

        // Process body based on content type
        if (contentType.startsWith("image/")) {
            // Handle image data (e.g., display it)
            System.out.println("Received image, Content-Type: " + contentType);
            RobotLog.d("LLIT5 processPart Image");
//            byte[] imgByteArray = Base64.decode(body, Base64.);
            byte[] imgByteArray = body.getBytes(StandardCharsets.UTF_8);

// try writting it to a file
            try (OutputStream outputStream = new FileOutputStream("/sdcard/FIRST/CW8620_b")) {
                outputStream.write(imgByteArray);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
// end try writting it to a file.


            Bitmap myBitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length);
            if (myBitmap != null) {
                FtcDashboard.getInstance().sendImage(myBitmap);
            }
            // Assuming body is the image data
            // DisplayImage(body);
        } else if (contentType.startsWith("text/")) {
            // Handle text data (e.g., update text field)
            System.out.println("Received text, Content-Type: " + contentType);
            RobotLog.d("LLIT5 processPart text");
            // UpdateText(body);
        } else {
            // Handle other content types or unknown types
            System.out.println("Received data, Content-Type: " + contentType);
            RobotLog.d("LLIT5 processPart Content-Type");

        }
    }



    public boolean streamToDashboard () {

        // todo  fix this baseUrl (debug and see what it is in the Limelight3A.java
        String baseUrl = "http://172.29.0.1:5800";
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

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
                RobotLog.d("LLIT3 cnt=" + cnt );
                    int available = is.available();
                    RobotLog.d("LLIT3 available=" + available );
                    cnt = cnt+available;
                    if (available>0) {
                        byte[] buffer = new byte[available];

                        // find beginning of jpg
                        is.read(buffer);
                        if (buffer[0] == 23) {
                            return false;
                        }
                    }




//                String response = readResponse5(connection);
//                if (isValidJson(response)) {
//                    return new JSONObject(response);
//                } else {
//                    JSONObject jsonObject = new JSONObject();
//                    JSONArray jsonArray = new JSONArray(response);
//                    jsonObject.put("fileNames", jsonArray);
//                    return jsonObject;
//                }
            } else {
                System.out.println("HTTP GET Error: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

        }
        return true;
    }

}
