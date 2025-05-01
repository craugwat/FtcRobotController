package org.firstinspires.ftc.teamcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.RobotLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LimeLightImageTools {
    Limelight3A limeLight;

    private String baseUrl = "http://0.0.0.0";
    private String ip = "0.0.0.0";




    LimeLightImageTools(Limelight3A limeLight) {
        this.limeLight = limeLight;
        try {
            Field privatIPaddress = Limelight3A.class.getDeclaredField("inetAddress");
            privatIPaddress.setAccessible(true);
            InetAddress ipAddress = (InetAddress) privatIPaddress.get(limeLight);
            InetAddress inetAddress = ipAddress;
            assert ipAddress != null;
            this.baseUrl = "http://" + ipAddress.getHostAddress();
            this.ip = ipAddress.getHostAddress();
        } catch (Exception e) {
            RobotLog.d("LLIT Failed to get IP address" );
        }
    }

    public Bitmap getSnapShotBMP() {
        String snapShotName = "snapshot";

        boolean captured = limeLight.captureSnapshot(snapShotName);

        // todo just a test to see if a time delay reduces drops
        long startTime = System.currentTimeMillis();
        while (startTime+40>System.currentTimeMillis());

        JSONObject obj = snapshotManifest();
        String snapShotFullName ="";

        if (obj != null) {
            try {
                snapShotFullName = findFullName(obj, snapShotName);
            } catch (JSONException e) {
                RobotLog.d("LLIT getSnapShotBMP findFullName failed" );
                return null;
            }
            if (snapShotFullName != "") {
                Bitmap snapShot = getBitmapFromSnapShot(snapShotFullName);
                if (snapShot != null) {
                    limeLight.deleteSnapshots();
                    return snapShot;
                }
            }
        }
        return null;
    }

    /**
     * find the first occurrence  of a string name in a JSONObject-JSONArray
     *
     * @param fileName full name of file we want to get from limelight snapshot directory
     * @return A Bitmap image if we get it from limelight, or return null
     */
    public Bitmap getBitmapFromSnapShot(String fileName) {
//        String imageUrl = "http://172.29.0.1:5801/snapshots/" + fileName; // Replace with your image URL
        String imageUrl = baseUrl + ":5801/snapshots/" + fileName; // Replace with your image URL
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
            RobotLog.d("LLIT findFullName Exception - " + e );

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

        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

        HttpURLConnection connection = null;
        try {
            String urlString = baseUrl + ":5807" + endpoint;
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
                RobotLog.d("LLIT sendGetRequest HTTP GET Error: " + responseCode);

            }
        } catch (Exception e) {
            RobotLog.d("LLIT sendGetRequest Exception - " + e );
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
//            RobotLog.d("LLIT isValidJson Exception - " + e );
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
            RobotLog.d("LLIT del Exception - " + e );
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


    // ***  Begin access pictures like the webpage does when PC plugged into camera  ***
    public enum Source {
        SNAPSHOT,
        RAW,
        PROCESSED
    }
    public Bitmap getBMP(Source source){
        switch (source) {
            case SNAPSHOT:
                return getSnapShotBMP();
            case RAW:
                return getMultiPartBMP(":5802");
            case PROCESSED:
                return getMultiPartBMP(":5800");
        }
        return null;
    }

    public  Bitmap getMultiPartBMP(String port)  {
        HttpURLConnection connection = openConnection(baseUrl + port);
        try {
            if (connection != null) {
                InputStream inputStream = connection.getInputStream();
                String contentType = connection.getHeaderField("Content-Type");
                String boundary = extractBoundary(contentType);
                if (boundary == null) {
                    RobotLog.d("LLIT boundary is NULL Exception - ");
                    return null;
                }
                return readImageFromStream(inputStream, boundary);
            }
        } catch (Exception e) {
            RobotLog.d("LLIT decodeMultipartImage Exception - " + e );
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private HttpURLConnection openConnection(String urlString) {
        int GETREQUEST_TIMEOUT = 100;
        int CONNECTION_TIMEOUT = 100;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(GETREQUEST_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            return connection;
        } catch (Exception e) {
            RobotLog.d("LLIT openConnection Exception - " + e );
        }
        return null;
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
    // ***  End access pictures like the webpage does when plugged into camera  ***


    // ***  Start try to do port forwarding through so can configure limelight through control hub
    public void portForwarding() {
        int localPort = 8620; // Port to listen on
        String remoteHost = ip; //"remote_host"; // Host to forward to
        int remotePort = 5800; // Port on remote host to forward to

        try {
            ServerSocket serverSocket = new ServerSocket(localPort);
            System.out.println("Listening on port " + localPort);
            RobotLog.d("LLIT portForwarding Listening on port " + localPort);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                RobotLog.d("LLIT portForwarding Accepted connection from " + clientSocket.getInetAddress());

                Thread forwardThread = new Thread(() -> {
                    try {
                        Socket remoteSocket = new Socket(remoteHost, remotePort);
                        System.out.println("Connected to remote host " + remoteHost + ":" + remotePort);
                        RobotLog.d("LLIT portForwarding Connected to remote host " + remoteHost + ":" + remotePort);

                        // Start threads to forward data in both directions
                        startForwarding(clientSocket.getInputStream(), remoteSocket.getOutputStream());
                        startForwarding(remoteSocket.getInputStream(), clientSocket.getOutputStream());

                    } catch (IOException e) {
                        System.err.println("Error forwarding: " + e.getMessage());
                        RobotLog.d("LLIT portForwarding Error forwarding: " + e.getMessage());

                    } finally {
//                        try {
//                            clientSocket.close();
//                            RobotLog.d("LLIT portForwarding client Socket Closed");
//                        } catch (IOException e) {
//                            // Ignore
//                        }
                    }
                });
                forwardThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            RobotLog.d("LLIT portForwarding Error starting server: " + e.getMessage());

        }
    }

    private static void startForwarding(InputStream input, OutputStream output) {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
            } catch (IOException e) {
                // Connection probably closed
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        });
        thread.start();
    }
    // ***  end try to do port forwarding through so can configure limelight through control hub
}
