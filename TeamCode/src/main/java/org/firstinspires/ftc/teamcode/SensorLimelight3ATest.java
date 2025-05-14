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

import com.acmerobotics.dashboard.FtcDashboard;
import com.example.limelighttools.LimeLightCameraStreamSource;
import com.example.limelighttools.LimeLightImageTools;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamServer;
import org.firstinspires.ftc.vision.VisionPortal;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvWebcam;


/**
 * This OpMode illustrates how to use the LimelightImageTools
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
        telemetry.update();


         LimeLightImageTools llIt = new LimeLightImageTools(limelight);
        //LimeLightImageTools llIt = new LimeLightImageTools("172.29.0.1");

        llIt.forwardAll();  // forward ports to allow remote communication with the limelight

        LimeLightCameraStreamSource streamSource = new LimeLightCameraStreamSource();
        FtcDashboard.getInstance().startCameraStream(streamSource,10);
        CameraStreamServer.getInstance().setSource(streamSource);

        waitForStart();

        int frames = 0;
        int droppedFrames = 0;
        boolean showProcessedImage = true;
        boolean lastAButton = false; // Store the state of the A button from the previous cycle

        long startTime = System.nanoTime();

        while (opModeIsActive()) {
            if (gamepad1.a && !lastAButton) {
                showProcessedImage = !showProcessedImage;
                frames = 0;
                droppedFrames = 0;
                startTime = System.nanoTime();
            }
            lastAButton = gamepad1.a;

            Bitmap bmp;
            if (showProcessedImage) {
                bmp = llIt.getProcessedBMP();
            } else {
                bmp = llIt.getRawBMP();
            }

            if (bmp != null) {
                FtcDashboard.getInstance().sendImage(bmp);
                frames++;
            } else {
                droppedFrames++;
            }
            long currentTime = System.nanoTime();
            double elapsedTimeSeconds = (currentTime - startTime) / 1_000_000_000.0;
            double frameRate =  (double)frames/elapsedTimeSeconds;

            RobotLog.d("LL_Test  " + (showProcessedImage ? "Processed":"Raw") + "  good frames = " + frames +"   Dropped frame = "+ droppedFrames + " Frame/second="+ frameRate);
//            telemetry.addData("BMP", bmp);
        }

    }
}

