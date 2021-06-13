// RobotBuilder Version: 3.1
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.

package frc.robot.Reference;

import frc.robot.Constants;
import frc.robot.commands.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.sensors.PigeonIMU;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 *
 */
public class DriveTrain_1519 extends SubsystemBase {

    private StringBuilder _sb = new StringBuilder();
    private int m_kPIDLoopIdx;
    private DoubleSolenoid dBL_Sol_Shifter;
    private WPI_TalonFX leftTalonMaster;
    private WPI_TalonFX leftTalonFollower1;
    private WPI_TalonFX leftTalonFollower2;
    private WPI_TalonFX rightTalonMaster;
    private WPI_TalonFX rightTalonFollower1;
    private WPI_TalonFX rightTalonFollower2;

    private PigeonIMU _pidgey = new PigeonIMU(0);
    private double[] xyz_dps = new double[3];
    private double currentAngle = 0;
    private boolean angleIsGood = false;
    private double currentAngularRate = xyz_dps[2];

    private static final int kMaxNumberOfMasterMotors = 2;
    private static final int kMaxNumberOfFollowerMotors = 4;
    private final int m_invertedMotors[] = new int[kMaxNumberOfMasterMotors];
    private static final int kLeft = 0;
    private static final int kRight = 1;

    private WPI_TalonFX[] m_talonsMaster = new WPI_TalonFX[kMaxNumberOfMasterMotors];
    private WPI_TalonFX[] m_talonsFollowers = new WPI_TalonFX[kMaxNumberOfFollowerMotors];
    private double m_wheelSpeeds[] = new double[kMaxNumberOfMasterMotors];
    private double m_zeroPositions[] = new double[kMaxNumberOfMasterMotors];
    private double m_wheeltargetPos[] = new double[kMaxNumberOfMasterMotors];

    private boolean m_useVoltageRamp = true;
    private double m_voltageRampRate = 36.0;// 48.0; // in volts/second
    private boolean m_fieldOrientedDrive = false;

    private int m_iterationsSinceRotationCommanded = 0;
    private double m_desiredHeading = 0.0;
    // private boolean m_drivingAutoInTeleop = false;

    // driving scaling factors
    private static final double FORWARD_BACKWARD_FACTOR = 1.0;

    // private static final double ROTATION_FACTOR_LOW_GEAR = 0.75;
    // private static final double ROTATION_FACTOR_HIGH_GEAR = 0.25;
    private static final double SLOW_FACTOR = 0.35;// 0.35; // scaling factor for (normal) "slow mode" .35
    private static final double CRAWL_INPUT = 0.30; // "crawl" is a gentle control input
    public static final double ALIGN_SPEED = 0.10;

    // member variables to support closed loop mode
    private boolean m_closedLoopMode = true;
    private ControlMode m_closedLoopMode2018;
    private double m_maxWheelSpeed_Current;
    private double m_maxWheelSpeed_HighGear = 747; // // 2016 = 445; //(10.5 Gear box = 445)//360(12.75 gear
                                                   // box);//550.0; // empirically measured around 560 to 580
    private double m_maxWheelSpeed_LowGear = 278;
    private double m_encoderUnitsPerRev = 4096;

    // Ramp rates in Seconds
    private double m_closedLoopRamp_sec = .25;
    private double m_openLoopRamp_sec = 0.0;

    // **************************************
    // NO GYRO ?
    // ************************************** */
    private boolean m_preserveHeading_Enable = true;
    private int m_preserveHeading_Iterations = 50;// 5 Original Driver Didn't like the snappy action
    private double kP_preserveHeading_Telepo = 0.005; // 0.025; Original Driver Didn't like the snappy action
    private double kP_preserveHeading_Auto = 0.025; // 0.025
    private boolean reportERROR_ONS = false;

    private boolean m_Craling = false;

    /**
    *
    */
    public DriveTrain_1519() {

        dBL_Sol_Shifter = new DoubleSolenoid(0, 4, 5);
        addChild("DBL_Sol_Shifter", dBL_Sol_Shifter);

        leftTalonMaster = new WPI_TalonFX(13);
        leftTalonMaster.configFactoryDefault();
        leftTalonFollower1 = new WPI_TalonFX(14);
        leftTalonFollower1.configFactoryDefault();
        leftTalonFollower2 = new WPI_TalonFX(15);
        leftTalonFollower2.configFactoryDefault();

        rightTalonMaster = new WPI_TalonFX(0);
        rightTalonMaster.configFactoryDefault();
        rightTalonFollower1 = new WPI_TalonFX(1);
        rightTalonFollower1.configFactoryDefault();
        rightTalonFollower2 = new WPI_TalonFX(2);
        rightTalonFollower2.configFactoryDefault();

        int talonIndex = 0;

        // construct the talons
        m_talonsMaster[kLeft] = leftTalonMaster;
        m_talonsMaster[kRight] = rightTalonMaster;

        // Group the Followers
        m_talonsFollowers[0] = leftTalonFollower1;
        m_talonsFollowers[1] = leftTalonFollower2;
        m_talonsFollowers[2] = rightTalonFollower1;
        m_talonsFollowers[3] = rightTalonFollower2;

        // set all Talon SRX encoder values to zero
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].setPosition(0);
            m_talonsMaster[talonIndex].setSelectedSensorPosition(0, 0, Constants.kTimeoutMs);
        }

        // set all the Talon feedback Devices
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
            m_talonsMaster[talonIndex].configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor,
                    Constants.kPIDLoopIdx, Constants.kTimeoutMs);
        }

        // Configure Nominal Output Voltage
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].configNominalOutputVoltage(+0.0f, -0.0f);
            m_talonsMaster[talonIndex].configNominalOutputForward(0, Constants.kTimeoutMs);
            m_talonsMaster[talonIndex].configNominalOutputReverse(0, Constants.kTimeoutMs);
        }

        // Configure Peak Output Voltage
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].configPeakOutputVoltage(+12.0f, -12.0f);
            m_talonsMaster[talonIndex].configPeakOutputForward(1, Constants.kTimeoutMs);
            m_talonsMaster[talonIndex].configPeakOutputReverse(-1, Constants.kTimeoutMs);
        }

        // put all Talon SRX into brake mode
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            m_talonsMaster[talonIndex].setNeutralMode(NeutralMode.Coast);

        }

        // put all Talon SRX into brake mode
        for (talonIndex = 0; talonIndex < kMaxNumberOfFollowerMotors; talonIndex++) {
            m_talonsFollowers[talonIndex].setNeutralMode(NeutralMode.Coast);

        }

        // ensure ramp rate set accordingly
        if (m_useVoltageRamp) {
            for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
                // m_talonsMaster[talonIndex].setVoltageRampRate(m_voltageRampRate);
                m_talonsMaster[talonIndex].configClosedloopRamp(m_closedLoopRamp_sec, Constants.kTimeoutMs);
            }
        } else {
            // clear all voltage ramp rates
            for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
                // m_talonsMaster[talonIndex].setVoltageRampRate(0.0);
                m_talonsMaster[talonIndex].configClosedloopRamp(m_openLoopRamp_sec, Constants.kTimeoutMs);
            }
        }

        // Also need to set up the "inverted motors" array for the mecanum drive
        // code
        m_invertedMotors[kLeft] = 1;
        m_invertedMotors[kRight] = -1;

        rightTalonFollower1.follow(rightTalonMaster);
        rightTalonFollower2.follow(rightTalonMaster);
        leftTalonFollower1.follow(leftTalonMaster);
        leftTalonFollower2.follow(leftTalonMaster);
    }

    public void init() {
        // complete initialization here that can't be performed in constructor
        // (some calls can't be made in constructor because other objects don't
        // yet exist)

        // Set up the TalonSRX closed loop / open loop mode for each wheel
        if (m_closedLoopMode) {
            setClosedLoopMode();
        } else {
            setOpenLoopMode();
        }

        shiftHigh();
    }

    @Override
    public void periodic() {
        // This method will be called once per scheduler run

    }

    @Override
    public void simulationPeriodic() {
        // This method will be called once per scheduler run when in simulation

    }

    // Put methods for controlling this subsystem
    // here. Call these from Commands.


    public boolean my_GetIsCurrentGearHigh() {
        if (dBL_Sol_Shifter.get() == Value.kReverse) {
            return true;

        } else {
            return false;
        }
    }

    public void shiftHigh() {
        DriverStation.reportWarning("Shift Highs Gear", false);
        dBL_Sol_Shifter.set(Value.kReverse);
        m_maxWheelSpeed_Current = m_maxWheelSpeed_HighGear;
        rightTalonMaster.selectProfileSlot(0, 0);
        leftTalonMaster.selectProfileSlot(0, 0);
        m_kPIDLoopIdx = 0;

        SmartDashboard.putBoolean("High Gear", true);
    }

    public void shiftLow() {
        DriverStation.reportWarning("Shift Low Gear", false);
        dBL_Sol_Shifter.set(Value.kForward);
        m_maxWheelSpeed_Current = m_maxWheelSpeed_LowGear;
        rightTalonMaster.selectProfileSlot(1, 0);
        leftTalonMaster.selectProfileSlot(1, 0);
        m_kPIDLoopIdx = 1;

        SmartDashboard.putBoolean("High Gear", false);
    }

    public double getMaxWheelSpeed() {
        return m_maxWheelSpeed_Current;
    }

    public void zeroDistanceTraveled() {
        int talonIndex = 0;
        // record current positions as "zero" for all of the Talon SRX encoders
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            m_zeroPositions[talonIndex] = (double) m_talonsMaster[talonIndex].getSelectedSensorPosition(0) / 2048;
        }
    }

    public double getDistanceTraveled() {
        int talonIndex = 0;
        double tempDistance = 0;
        double realDistance = 0;
        // double leftRawRotations = (double)
        // m_talons[kLeft].getSelectedSensorPosition(0) / 4096;
        // double right = (double) m_talons[kRight].getSelectedSensorPosition(0) / 4096;

        // add up the absolute value of the distances from each individual wheel
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            tempDistance += Math.abs(((double) m_talonsMaster[talonIndex].getSelectedSensorPosition(0) / 2048)
                    - m_zeroPositions[talonIndex]);
        }

        realDistance = (tempDistance * (6.125 * 3.14)) / 2;
        return realDistance;// (tempDistance);
    }

    public void setWheelPIDF() {
        int talonIndex = 0;
        double wheelP_HighGear = 0.0;// 0.5;
        double wheelI_HighGear = 0.0;
        double wheelD_HighGear = 0.0;
        double wheelF_HighGear = 0.1967;

        double wheelP_LowGear = 0.0;// 1.5;
        double wheelI_LowGear = 0.0;
        double wheelD_LowGear = 0.0;
        double wheelF_LowGear = 0.5384;

        double wheelP_MotionMagic = 0.3;// 0.5;
        double wheelI_MotionMagic = 0.0;
        double wheelD_MotionMagic = 0.0;
        double wheelF_MotionMagic = 0.1967;

        // set the PID values for each individual wheel
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talons[talonIndex].setPID(wheelP, wheelI, wheelD, wheelF, 0,
            // m_voltageRampRate, 0);
            m_talonsMaster[talonIndex].config_kP(0, wheelP_HighGear, 0);
            m_talonsMaster[talonIndex].config_kI(0, wheelI_HighGear, 0);
            m_talonsMaster[talonIndex].config_kD(0, wheelD_HighGear, 0);
            m_talonsMaster[talonIndex].config_kF(0, wheelF_HighGear, 0);

            m_talonsMaster[talonIndex].config_kP(1, wheelP_LowGear, 0);
            m_talonsMaster[talonIndex].config_kI(1, wheelI_LowGear, 0);
            m_talonsMaster[talonIndex].config_kD(1, wheelD_LowGear, 0);
            m_talonsMaster[talonIndex].config_kF(1, wheelF_LowGear, 0);

            m_talonsMaster[talonIndex].config_kP(2, wheelP_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kI(2, wheelI_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kD(2, wheelD_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kF(2, wheelF_MotionMagic, 0);
        }
        DriverStation.reportError("setWheelPIDF:\n", false);
    }

    private void getPidgey() {
        /* some temps for Pigeon API */
        PigeonIMU.GeneralStatus genStatus = new PigeonIMU.GeneralStatus();
        PigeonIMU.FusionStatus fusionStatus = new PigeonIMU.FusionStatus();

        /* grab some input data from Pigeon and gamepad */
        _pidgey.getGeneralStatus(genStatus);
        _pidgey.getRawGyro(xyz_dps);
        _pidgey.getFusedHeading(fusionStatus);
        currentAngle = fusionStatus.heading;
        angleIsGood = (_pidgey.getState() == PigeonIMU.PigeonState.Ready) ? true : false;
        currentAngularRate = xyz_dps[2];
    }

    public void setgyroOffset(double adjustment) {
        // Follow up headingGyro.setAngleAdjustment(adjustment);
        // headingGyro_BCK.setAngledAdjustimenet(adjustment); // Not available
        _pidgey.setFusedHeading(adjustment);
        // _pidgey.setYaw(adjustment);
    }

    public double getHeading() {
        getPidgey();
        double heading;
        if (angleIsGood) {
            heading = currentAngle;
        } else {
            heading = 0;// headingGyro_BCK.getAngle() + headingGyro.getAngleAdjustment();//Try to use
                        // the Back up Gyro with the angle Adjustment
        }

        return heading;
        // return headingGyro.getFusedHeading();
    }

    public void resetHeadingGyro() {
        _pidgey.setFusedHeading(0);
        m_desiredHeading = 0.0;
    }

    public void clearDesiredHeading() {
        m_desiredHeading = getHeading();
    }

    public void setdesiredHeading(double heading) {
        m_desiredHeading = heading;
    }

    public void recalibrateHeadingGyro() {
        resetHeadingGyro();
    }

    public void setFieldOrientedDrive(boolean enable) {

        m_fieldOrientedDrive = enable;
        // SmartDashboard.putBoolean("Field Oriented Drive", m_fieldOrientedDrive);

    }

    public void toggleFieldOrientedDrive() {
        m_fieldOrientedDrive = !m_fieldOrientedDrive;
        // SmartDashboard.putBoolean("Field Oriented Drive", m_fieldOrientedDrive);
    }

    public void setClosedLoopMode() {
        m_closedLoopMode2018 = ControlMode.Velocity;

        int talonIndex = 0;
        m_closedLoopMode = true;
        setWheelPIDF();
        /*
         * for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
         * m_talons[talonIndex].changeControlMode(TalonControlMode.Speed);
         * m_talons[talonIndex].enableControl();
         * 
         * }
         */

    }

    public void setOpenLoopMode() {
        m_closedLoopMode2018 = ControlMode.PercentOutput;
        /*
         * int talonIndex = 0; m_closedLoopMode = false; for (talonIndex = 0; talonIndex
         * < kMaxNumberOfMasterMotors; talonIndex++) {
         * m_talons[talonIndex].changeControlMode(TalonControlMode.PercentVbus);
         * m_talons[talonIndex].enableControl(); }
         */
    }

    public int getLoopMode(int talonIndex) {
        if (talonIndex < kMaxNumberOfMasterMotors) {
            // return m_talons[talonIndex].getControlMode().getValue();
            return m_talonsMaster[talonIndex].getControlMode().value;
        } else {
            return 0;
        }

    }

    public void toggleClosedLoopMode() {
        if (!m_closedLoopMode) {
            setClosedLoopMode();
        } else {
            setOpenLoopMode();
        }
    }

    public void set_PreserveHeading(boolean set) {
        if (set) {
            m_preserveHeading_Enable = true;
            m_iterationsSinceRotationCommanded = 0;
        } else {
            m_preserveHeading_Enable = false;
        }

        // ******* Per Driver request m_preserveHeading_Enable = false;
        // m_preserveHeading_Enable = false;
    }

    /**
     * Normalize all wheel speeds if the magnitude of any wheel is greater than 1.0.
     */
    private void normalizeAndScaleWheelSpeeds() {
        int i;
        double tempMagnitude;
        double maxMagnitude;

        // SmartDashboard.putNumber("a_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
        // SmartDashboard.putNumber("a_wheelSpeeds[kRight]", m_wheelSpeeds[kRight]);
        // find maxMagnitude
        maxMagnitude = Math.abs(m_wheelSpeeds[0]);
        for (i = 1; i < kMaxNumberOfMasterMotors; i++) {
            tempMagnitude = Math.abs(m_wheelSpeeds[i]);
            if (tempMagnitude > maxMagnitude) {
                maxMagnitude = tempMagnitude;
            }
        }

        // SmartDashboard.putNumber("maxMagnitude", maxMagnitude);
        // if any wheel has a magnitude greater than 1.0, reduce all to fit in
        // range
        if (maxMagnitude > 1.0) {
            for (i = 0; i < kMaxNumberOfMasterMotors; i++) {
                m_wheelSpeeds[i] = m_wheelSpeeds[i] / maxMagnitude;
            }
        }
        // SmartDashboard.putNumber("b_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
        // SmartDashboard.putNumber("b_wheelSpeeds[kRight]", m_wheelSpeeds[kRight]);
        // if in closedLoopMode, scale wheels to be speeds, rather than power
        // percentage
        if (m_closedLoopMode) {
            for (i = 0; i < kMaxNumberOfMasterMotors; i++) {
                // SmartDashboard.putNumber("c_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
                // SmartDashboard.putNumber("c_wheelSpeeds[krigt]", m_wheelSpeeds[kRight]);
                /* Speed mode */
                /*
                 * 4096 Units/Rev * 500 RPM / 600 100ms/min in either direction: velocity
                 * setpoint is in units/100ms
                 */
                m_wheelSpeeds[i] = m_wheelSpeeds[i] * m_maxWheelSpeed_Current * m_encoderUnitsPerRev / 600;
                // SmartDashboard.putNumber("m_maxWheelSpeed_Current", m_maxWheelSpeed_Current);
                // SmartDashboard.putNumber("d_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
                // SmartDashboard.putNumber("d_wheelSpeeds[kRight]", m_wheelSpeeds[kRight]);
            }
        }
    }

    /**
     * Correct any inverted motors
     */
    private void correctInvertedMotors() {
        int i;

        for (i = 0; i < kMaxNumberOfMasterMotors; i++) {
            m_wheelSpeeds[i] = m_wheelSpeeds[i] * m_invertedMotors[i];
        }
    }

    public void driveTeleop(double xSpeed, double xRotation, DoubleSupplier turnScaller) {
        driveTeleop(xSpeed, xRotation, true, true, turnScaller);
    }

    /**
     * Drive method for Mecanum wheeled robots.
     *
     * A method for driving with Mecanum wheeled robots. There are 4 wheels on the
     * robot, arranged so that the front and back wheels are toed in 45 degrees.
     * When looking at the wheels from the top, the roller axles should form an X
     * across the robot.
     *
     * This is designed to be directly driven by joystick axes.
     *
     * @param x        The speed that the robot should drive in the X direction.
     *                 [-1.0..1.0]
     * @param y        The speed that the robot should drive in the Y direction.
     *                 [-1.0..1.0]
     * @param rotation The rate of rotation for the robot that is completely
     *                 independent of the translation. [-1.0..1.0]
     */
    public void driveTeleop(double xSpeed, double xRotation, boolean square_xSpeedputs, boolean square_rInputs,
            DoubleSupplier turnScaller) {

        // check for the presence of the special "crawl" commands and do those
        // if commanded
        // if (Robot.oi.crawlBackward()) {
        // xSpeed = -CRAWL_INPUT;
        // rotation = rotation * .5;
        // m_Craling = true;
        // }else if (Robot.oi.crawlForward()) {
        // xSpeed = CRAWL_INPUT;
        // rotation = rotation * .5;
        // m_Craling = true;
        // }else{
        // m_Craling = false;
        // }

        // Disable Field Oriantated if Gyro Fails
        boolean IMU_Connected = true;// headingGyro.isConnected();
        if (!IMU_Connected) {
            m_preserveHeading_Enable = false;
            m_fieldOrientedDrive = false;
            // SmartDashboard.putBoolean("Field Oriented Drive", m_fieldOrientedDrive);
            if (!reportERROR_ONS) {
                DriverStation.reportError("Lost Gyro - Forcing Robot Oriantated " + "\n", false);
                reportERROR_ONS = true;
            }

        }

        // check to see if forward/back, and rotation are being
        // commanded.
        // values with magnitude < 0.07 are just "centering noise" and set to
        // 0.0

        if ((-0.07 < xSpeed) && (xSpeed < 0.07)) {
            xSpeed = 0.0;
        } else {
            // xSpeed = xSpeed * FORWARD_BACKWARD_FACTOR;
            if (square_xSpeedputs) {
                xSpeed = Math.copySign(xSpeed * xSpeed, xSpeed);
            }
        }
        double MaxDriveSpeed = SmartDashboard.getNumber("MaxDriveSpeed", .4);
        if (Math.abs(xSpeed) > MaxDriveSpeed) {
            xSpeed = Math.copySign(MaxDriveSpeed, xSpeed);
        }

        double turnScaler = turnScaller.getAsDouble();

        if (turnScaler < .2) {
            turnScaler = .2;
        }

        // Scall the Rotation Factor
        if ((-0.07 < xRotation) && (xRotation < 0.07)) {
            xRotation = 0.0;
        } else {
            if (square_rInputs) {
                xRotation = Math.copySign(xRotation * xRotation, xRotation);
            }
            // rotation = rotation; // .15 is a FeedForward
            if (my_GetIsCurrentGearHigh()) {

                xRotation = xRotation * turnScaler;
                // if(Math.abs(rotation) < 0.95){
                // rotation = rotation * turnScaler;//ROTATION_FACTOR_LOW_GEAR;
                // }else{
                // rotation = rotation * .55;//ROTATION_FACTOR_HIGH_GEAR;
                // }

            } else { // In low gear

                if (Math.abs(xRotation) < 0.95) {
                    xRotation = xRotation * turnScaler;// ROTATION_FACTOR_LOW_GEAR;
                } else {
                    xRotation = xRotation * turnScaler;
                }
            }
        }

        // apply "slowFactor" if not in "Turbo Mode"
        // if (!Robot.oi.driveTurboMode() || !m_Craling) {
        // xIn = xIn * 1.0;//SLOW_FACTOR;
        // xSpeed = xSpeed * 1.0;//SLOW_FACTOR;

        // }

        // update count of iterations since rotation last commanded
        if ((-0.01 < xRotation) && (xRotation < 0.01)) {
            // rotation is practically zero, so just set it to zero and
            // increment iterations
            xRotation = 0.0;
            m_iterationsSinceRotationCommanded++;
        } else {
            // rotation is being commanded, so clear iteration counter
            m_iterationsSinceRotationCommanded = 0;
        }

        // preserve heading when recently stopped commanding rotations
        if (m_iterationsSinceRotationCommanded == m_preserveHeading_Iterations) {
            m_desiredHeading = getHeading();
        } else if (m_iterationsSinceRotationCommanded > m_preserveHeading_Iterations) {
            if (m_preserveHeading_Enable) {
                xRotation = (m_desiredHeading - getHeading()) * kP_preserveHeading_Telepo;
                // SmartDashboard.putNumber("MaintainHeaading ROtation", rotation);
            }
        }

        // if(rotation>.3){
        // rotation=.3;
        // }
        // if(rotation<-.3){
        // rotation=-.3;
        // }

        driveCartesian(xSpeed, xRotation);
    }

    private void driveAutonomous(double xSpeed, double xRotation, double heading) {
        m_desiredHeading = heading;

        // preserve heading if no rotation is commanded
        if ((-0.01 < xRotation) && (xRotation < 0.01)) {
            xRotation = (m_desiredHeading - getHeading()) * kP_preserveHeading_Auto; // In Auto keep the snappy action

        }

        double limit = .3;
        if (xRotation > limit) {
            xRotation = limit;
        } else if (xRotation < -limit) {
            xRotation = -limit;
        }

        // SmartDashboard.putNumber("m_desiredHeading", m_desiredHeading);
        // SmartDashboard.putNumber("getHeading",getHeading());
        // SmartDashboard.putNumber("rotation", rotation);
        driveCartesian(xSpeed, xRotation);
    }

    // public void driveAutoInTeleopFinished() {
    // m_drivingAutoInTeleop = false;
    // }

    private void driveAutoInTeleop(double xSpeed, double xRotation) {
        // m_drivingAutoInTeleop = true;

        // update count of iterations since rotation last commanded
        if ((-0.01 < xRotation) && (xRotation < 0.01)) {
            // xRotation is practically zero, so just set it to zero and
            // increment iterations
            xRotation = 0.0;
            m_iterationsSinceRotationCommanded++;
        } else {
            // rotation is being commanded, so clear iteration counter
            m_iterationsSinceRotationCommanded = 0;
        }

        // preserve heading when recently stopped commanding rotations
        if (m_preserveHeading_Enable && m_iterationsSinceRotationCommanded == 5) {
            m_desiredHeading = getHeading();
        } else if (m_iterationsSinceRotationCommanded > 5) {
            if (m_preserveHeading_Enable) {
                xRotation = (m_desiredHeading - getHeading()) * kP_preserveHeading_Auto; // In Auto keep the snappy
                                                                                         // action
            }
        }

        driveCartesian(xSpeed, xRotation);
    }

    private void driveCartesian(double xSpeed, double xRotation) {
        int talonIndex = 0;

        m_wheelSpeeds[kLeft] = -xSpeed + xRotation;
        m_wheelSpeeds[kRight] = -xSpeed - xRotation;

        normalizeAndScaleWheelSpeeds();
        correctInvertedMotors();

        // want to do all the sets immediately after one another to minimize
        // delay between commands
        // set all Talon SRX encoder values to zero
        // SmartDashboard.putNumber("Left talon", m_wheelSpeeds[kLeft]);
        // SmartDashboard.putNumber("Right talon", m_wheelSpeeds[kRight]);
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            m_talonsMaster[talonIndex].set(m_closedLoopMode2018, m_wheelSpeeds[talonIndex]);
        }
        // m_talons[0].set(m_closedLoopMode2018, m_wheelSpeeds[0]);
        // m_talons[1].set(m_closedLoopMode2018, m_wheelSpeeds[1]);

    }

    public boolean get_my_Gyro_IsReady() {
        if (_pidgey.getState().value == 2) {
            return true;
        } else {
            return false;
        }
    }

    public void diagnostics() {

        SmartDashboard.putNumber("Heading", getHeading());
        // SmartDashboard.putNumber("Encoder Distance", getDistanceTraveled());

    }

}