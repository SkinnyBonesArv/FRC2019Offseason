package com.team175.robot.command;

import com.team175.robot.subsystem.Drive;
import com.team175.robot.subsystem.Limelight;
import edu.wpi.first.wpilibj.Timer;

/**
 * DriveToTarget uses the limelight to continuously calculate the throttle and turn needed to reach the retro reflective
 * tape targets and feeds the outputs into the drive motors.
 */
public final class DriveToVisionTarget extends CommandBase {

    private final Drive drive;
    private final Limelight limelight;

    public DriveToVisionTarget(Drive drive, Limelight limelight) {
        this.drive = drive;
        this.limelight = limelight;
        addRequirements(this.drive, this.limelight);
    }

    @Override
    public void initialize() {
        limelight.setCameraMode(true);
        limelight.turnOnLED();
        drive.setOpenLoop(0, 0);
        // Timer.delay(1);
    }

    @Override
    public void execute() {
        /*double[] driveSignal = limelight.calculateTargetDrive();
        // driveSignal[0] => Throttle, driveSignal[1] => Turn
        drive.arcadeDrive(driveSignal[0], driveSignal[1]);*/

        limelight.calculateTargetDrive();
        drive.arcadeDrive(limelight.getThrottle(), limelight.getTurn());
    }

    @Override
    public void end(boolean interrupted) {
        limelight.setCameraMode(false);
        // limelight.defaultLED();
        // limelight.setLED(false);
    }

    @Override
    public boolean isFinished() {
        return limelight.isAtTarget();
    }

}
