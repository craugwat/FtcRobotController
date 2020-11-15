package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcontroller.external.samples.HardwarePushbot;
@TeleOp(name="X-wing-TeleOp", group="Pushbot")

public class xWingTele  extends OpMode {
    /* Declare OpMode members. */
    Hardware_xWing robot  = new Hardware_xWing(); // use the class created to define a Pushbot's hardware

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap) ;

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Hello Driver");    //

    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    double maxVel = 0.5;
    @Override
    public void loop() {

        double frontLeft;
        double backLeft;
        double frontRight;
        double backRight;
        double max;
        double x_axis = -gamepad1.left_stick_x * maxVel;
        double y_axis = -gamepad1.left_stick_y * maxVel;
        double x_prime;
        double y_prime;
        double theta = Math.toRadians(-robot.getZangle());
        double gyroHeading = robot.getZangle() ;

        // Run wheels in POV mode (note: The joystick goes negative when pushed forwards, so negate it)
        // In this mode the Left stick moves the robot fwd and back, the Right stick turns left and right.
        //  Find robot's current axes in relation to original axes
        x_prime = x_axis * Math.cos(theta) + y_axis * Math.sin(theta);
        y_prime = -x_axis * Math.sin(theta) + y_axis * Math.cos(theta);

        frontRight  = y_prime - (gamepad1.right_stick_x / 2 * maxVel) + x_prime;
        backRight   = y_prime - (gamepad1.right_stick_x / 2 * maxVel) - x_prime;
        frontLeft   = y_prime + (gamepad1.right_stick_x / 2 * maxVel) - x_prime;
        backLeft    = y_prime + (gamepad1.right_stick_x / 2 * maxVel) + x_prime;

        // Run wheels in POV mode (note: The joystick goes negative when pushed forwards, so negate it)
        // In this mode the Left stick moves the robot fwd and back, the Right stick turns left and right.
        frontRight  = y_prime - (gamepad1.right_stick_x / 2 * maxVel) + x_prime;
        backRight   = y_prime - (gamepad1.right_stick_x / 2 * maxVel) - x_prime;
        frontLeft   = y_prime + (gamepad1.right_stick_x / 2 * maxVel) - x_prime;
        backLeft    = y_prime + (gamepad1.right_stick_x / 2 * maxVel) + x_prime;

        if (gamepad1.left_trigger > .05)
            maxVel = 0.5;
        else if (gamepad1.right_trigger > .05)
            maxVel = 1.0;
        else if (gamepad1.left_bumper)
            maxVel = 0.25;
        // Normalize the values so neither exceed +/- 1.0
        max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(backLeft)), Math.max(Math.abs(frontRight), Math.abs(backRight)));
        if (max > 1) {
            frontLeft /= max;
            backLeft /= max;
            frontRight /= max;
            backRight /= max;
        }

        robot.frontLeftDrive.setPower(frontLeft);
        robot.frontRightDrive.setPower(frontRight);
        robot.backLeftDrive.setPower(backLeft);
        robot.backRightDrive.setPower(backRight);

        if (gamepad1.y)  {
            robot.resetHeading();
        }

//        if (gamepad1.y)
//            robot.frontRightDrive.setPower(0.2);
//        else
//            robot.frontRightDrive.setPower(0.0);
//
//
//        if (gamepad1.b)
//            robot.backRightDrive.setPower(0.2);
//        else
//            robot.backRightDrive.setPower(0.0);
//
//        if (gamepad1.a)
//            robot.backLeftDrive.setPower(0.2);
//        else
//            robot.backLeftDrive.setPower(0.0);
//
//        if (gamepad1.x)
//            robot.frontLeftDrive.setPower(0.2);
//        else
//            robot.frontLeftDrive.setPower(0.0);

        telemetry.addData("frontRightDrive pos",robot.frontRightDrive.getCurrentPosition());
        telemetry.addData("backRightDrive pos",robot.backRightDrive.getCurrentPosition());
        telemetry.addData("backLeftDrive pos",robot.backLeftDrive.getCurrentPosition());
        telemetry.addData("frontLeftDrive pos",robot.frontLeftDrive.getCurrentPosition());
        telemetry.addData("Gyro Heading", gyroHeading);
        telemetry.addData("Theta (in radians)", theta);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }

}
