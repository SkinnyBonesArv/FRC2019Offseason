package com.team175.robot.subsystem;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Limelight represents the limelight vision processing unit on the robot.
 */
public final class Limelight extends SubsystemBase {

    private final NetworkTable table;

    private double throttle, turn;
    private boolean isAtTarget;

    private static final int WANTED_TARGET_AREA = 14;
    private static final int ROTATION_DEADBAND = 4;
    private static final double AREA_DEADBAND = 1.5;
    private static final double KP_THROTTLE = 0.2;
    private static final double KP_TURN = 0.05;
    private static final double MAX_THROTTLE = 0.8;
    private static final double SEEK_TURN = 0.3;

    private static Limelight instance;

    private Limelight() {
        table = NetworkTableInstance.getDefault().getTable("limelight");

        configureTelemetry();
    }

    public static Limelight getInstance() {
        if (instance == null) {
            instance = new Limelight();
        }

        return instance;
    }

    private void configureTelemetry() {
        telemetry.put("IsTargetDetected", this::isTargetDetected);
        telemetry.put("HorizontalOffset", this::getHorizontalOffset);
        telemetry.put("VerticalOffset", this::getVerticalOffset);
        telemetry.put("TargetArea", this::getTargetArea);
        telemetry.put("Rotation", this::getRotation);
        telemetry.put("PipelineNum", this::getPipeline);
        telemetry.put("CalculatedThrottle", () -> throttle);
        telemetry.put("CalculatedTurn", () -> turn);
    }

    private void setPipeline(int pipelineNum) {
        table.getEntry("pipeline").setNumber(pipelineNum);
    }

    private boolean isTargetDetected() {
        return table.getEntry("tv").getDouble(0) == 1;
    }

    private double getHorizontalOffset() {
        return table.getEntry("tx").getDouble(0);
    }

    private double getVerticalOffset() {
        return table.getEntry("ty").getDouble(0);
    }

    private double getTargetArea() {
        return table.getEntry("ta").getDouble(0);
    }

    private double getRotation() {
        return table.getEntry("ts").getDouble(0);
    }

    private int getPipeline() {
        return (int) table.getEntry("getpipe").getDouble(0);
    }

    public void setLED(boolean enable) {
        table.getEntry("ledMode").setNumber(enable ? 3 : 1);
    }

    public void blinkLED() {
        table.getEntry("ledMode").setNumber(2);
    }

    public void defaultLED() {
        table.getEntry("ledMode").setNumber(0);
    }

    public void setCameraMode(boolean isTrackingMode) {
        // 0 => Tracking Mode; 1 => Driver Mode
        setPipeline(isTrackingMode ? 0 : 1);
        // table.getEntry("camMode").setNumber(isTrackingMode ? 0 : 1);
    }

    public boolean isAtTarget() {
        return isAtTarget;
    }

    public double[] calculateTargetDrive() {
        if (isTargetDetected()) {
            // Proportional turn based on tx
            turn = KP_TURN * getHorizontalOffset();
            // Proportional throttle based on ta
            // min() prevents robot from driving too fast and crashing
            throttle = Math.min(((WANTED_TARGET_AREA - getTargetArea()) * KP_THROTTLE), MAX_THROTTLE);

            isAtTarget = getHorizontalOffset() <= ROTATION_DEADBAND
                    && Math.abs(WANTED_TARGET_AREA - getTargetArea()) <= AREA_DEADBAND;

            logger.debug("Throttle = {}", throttle);
            logger.debug("Turn = {}", turn);
            logger.debug("IsAtTarget = {}", isAtTarget);
        } else {
            throttle = 0;
            turn = SEEK_TURN;
            isAtTarget = false;
            logger.warn("NO TARGET DETECTED!!! Robot is turning in a circle until it sees a target.");
        }

        return new double[]{throttle, turn};
    }

    @Override
    public void resetSensors() {
        setPipeline(0);
        defaultLED();
        setCameraMode(false);
    }

    @Override
    public boolean checkIntegrity() {
        return false;
    }

}
