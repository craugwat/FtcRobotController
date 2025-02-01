/*
Copyright (c) 2024 Limelight Vision

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of FIRST nor the names of its contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/*
 * This OpMode illustrates how to use the Limelight3A Vision Sensor.
 *
 * @see <a href="https://limelightvision.io/">Limelight</a>
 *
 * Notes on configuration:
 *
 *   The device presents itself, when plugged into a USB port on a Control Hub as an ethernet
 *   interface.  A DHCP server running on the Limelight automatically assigns the Control Hub an
 *   ip address for the new ethernet interface.
 *
 *   Since the Limelight is plugged into a USB port, it will be listed on the top level configuration
 *   activity along with the Control Hub Portal and other USB devices such as webcams.  Typically
 *   serial numbers are displayed below the device's names.  In the case of the Limelight device, the
 *   Control Hub's assigned ip address for that ethernet interface is used as the "serial number".
 *
 *   Tapping the Limelight's name, transitions to a new screen where the user can rename the Limelight
 *   and specify the Limelight's ip address.  Users should take care not to confuse the ip address of
 *   the Limelight itself, which can be configured through the Limelight settings page via a web browser,
 *   and the ip address the Limelight device assigned the Control Hub and which is displayed in small text
 *   below the name of the Limelight on the top level configuration screen.
 */
@TeleOp(name = "Sensor: Limelight3A", group = "Sensor")
//@Disabled
public class SensorLimelight3ATest extends LinearOpMode {

    private Limelight3A limelight;

    @Override
    public void runOpMode() throws InterruptedException
    {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11);

        limelight.pipelineSwitch(0);

        /*
         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
         */
        limelight.start();
        limelight.deleteSnapshots();
        telemetry.addData(">", "Robot Ready.  Press Play.");
//        telemetry.addData(">", limelight.getConnectionInfo());
        telemetry.update();


        waitForStart();

        // caw test begin

        FtcDashboard dashboard = FtcDashboard.getInstance();

        String snapShotName = "craug";
        while (opModeIsActive()) {
            boolean captured = limelight.captureSnapshot(snapShotName);
            telemetry.addData(">snapshot?", captured);

            JSONObject obj = snapshotManifest();
            String snapShotFullName ="";

            if (obj != null) {
                try {
                    snapShotFullName = findFullName(obj, snapShotName);
                } catch (JSONException e) {
                    snapShotFullName = "";
                }
                if (snapShotFullName != "") {
                    Bitmap snapShot = getBitmapFromSnapShot(snapShotFullName);
                    if (snapShot != null) {
                        dashboard.sendImage(snapShot);
                        limelight.deleteSnapshots();
                    }
                }
            }

            telemetry.addData(">name ", snapShotName);
            telemetry.addData(">", obj);
            telemetry.update();
            sleep(10);
        }

        sleep(2000);

//        // caw test end
//
//
//        while (opModeIsActive()) {
//            LLStatus status = limelight.getStatus();
//            telemetry.addData("Name", "%s",
//                    status.getName());
//            telemetry.addData("LL", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
//                    status.getTemp(), status.getCpu(),(int)status.getFps());
//            telemetry.addData("Pipeline", "Index: %d, Type: %s",
//                    status.getPipelineIndex(), status.getPipelineType());
//
//            LLResult result = limelight.getLatestResult();
//            if (result != null) {
//                // Access general information
//                Pose3D botpose = result.getBotpose();
//                double captureLatency = result.getCaptureLatency();
//                double targetingLatency = result.getTargetingLatency();
//                double parseLatency = result.getParseLatency();
//                telemetry.addData("LL Latency", captureLatency + targetingLatency);
//                telemetry.addData("Parse Latency", parseLatency);
//                telemetry.addData("PythonOutput", java.util.Arrays.toString(result.getPythonOutput()));
//
//                if (result.isValid()) {
//                    telemetry.addData("tx", result.getTx());
//                    telemetry.addData("txnc", result.getTxNC());
//                    telemetry.addData("ty", result.getTy());
//                    telemetry.addData("tync", result.getTyNC());
//
//                    telemetry.addData("Botpose", botpose.toString());
//
//                    // Access barcode results
//                    List<LLResultTypes.BarcodeResult> barcodeResults = result.getBarcodeResults();
//                    for (LLResultTypes.BarcodeResult br : barcodeResults) {
//                        telemetry.addData("Barcode", "Data: %s", br.getData());
//                    }
//
//                    // Access classifier results
//                    List<LLResultTypes.ClassifierResult> classifierResults = result.getClassifierResults();
//                    for (LLResultTypes.ClassifierResult cr : classifierResults) {
//                        telemetry.addData("Classifier", "Class: %s, Confidence: %.2f", cr.getClassName(), cr.getConfidence());
//                    }
//
//                    // Access detector results
//                    List<LLResultTypes.DetectorResult> detectorResults = result.getDetectorResults();
//                    for (LLResultTypes.DetectorResult dr : detectorResults) {
//                        telemetry.addData("Detector", "Class: %s, Area: %.2f", dr.getClassName(), dr.getTargetArea());
//                    }
//
//                    // Access fiducial results
//                    List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
//                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
//                        telemetry.addData("Fiducial", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(),fr.getTargetXDegrees(), fr.getTargetYDegrees());
//                    }
//
//                    // Access color results
//                    List<LLResultTypes.ColorResult> colorResults = result.getColorResults();
//                    for (LLResultTypes.ColorResult cr : colorResults) {
//                        telemetry.addData("Color", "X: %.2f, Y: %.2f", cr.getTargetXDegrees(), cr.getTargetYDegrees());
//                    }
//                }
//            } else {
//                telemetry.addData("Limelight", "No data available");
//            }
//
//            telemetry.update();
//        }
//        limelight.stop();
    }

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
    private JSONObject snapshotManifest() {
        return sendGetRequest("/snapshotmanifest");
    }

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

    public Bitmap getBitmapFromSnapShot(String fileName) {
        String imageUrl = "http://172.29.0.1:5801/snapshots/" + fileName; // Replace with your image URL

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
            return null;
        }
    }

    // find the first occurrence  of a string name
    // return the full string name, limelight adds some characters!
    String findFullName(JSONObject obj, String snapShotName) throws JSONException {
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

}

