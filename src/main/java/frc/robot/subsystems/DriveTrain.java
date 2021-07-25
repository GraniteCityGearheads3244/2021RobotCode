// RobotBuilder Version: 3.1
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.

package frc.robot.subsystems;

import frc.robot.Constants;
import frc.robot.commands.*;
import frc.robot.util.Utils;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpiutil.math.MathUtil;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
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
public class DriveTrain extends SubsystemBase {

    private static final double kDefaultDeadband = 0.2;
    private static final double kDefaultMaxOutput = 1.0;

    protected double m_deadband = kDefaultDeadband;
    protected double m_maxOutput = kDefaultMaxOutput;

    private StringBuilder _sb = new StringBuilder();
    private int m_kPIDLoopIdx;
    private DoubleSolenoid dBL_Sol_Shifter;
    private Value HIGHGEAR_VALUE = Value.kForward;
    private Value LOWGEAR_VALUE = Value.kReverse;
    private WPI_TalonFX leftTalonMaster;
    private WPI_TalonFX leftTalonFollower1;
    private WPI_TalonFX leftTalonFollower2;
    private WPI_TalonFX rightTalonMaster;
    private WPI_TalonFX rightTalonFollower1;
    private WPI_TalonFX rightTalonFollower2;

    private PigeonIMU pigeonIMU1;
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
    private static final double TURN_FACTOR = 0.3;

    // private static final double ROTATION_FACTOR_LOW_GEAR = 0.75;
    // private static final double ROTATION_FACTOR_HIGH_GEAR = 0.25;
    private static final double SLOW_FACTOR = 0.35;// 0.35; // scaling factor for (normal) "slow mode" .35
    private static final double CRAWL_INPUT = 0.30; // "crawl" is a gentle control input
    public static final double ALIGN_SPEED = 0.10;

    // member variables to support closed loop mode
    private boolean m_closedLoopMode = true;
    private TalonFXControlMode m_closedLoopTalonFXMode;
    private double m_maxWheelSpeed_Current;
    private double m_maxWheelSpeed_HighGear = 20660; // TalonFX intergrated Encoder// // 2016 = 445; //(10.5 Gear box =
                                                     // 445)//360(12.75 gear
                                                     // box);//550.0; // empirically measured around 560 to 580
    private double m_maxWheelSpeed_LowGear = 20660;
    private double m_encoderUnitsPerRev = 2048;

    // Ramp rates in Seconds
    private double m_closedLoopRamp_sec = .15;
    private double m_closedLoopRamp_Demo_sec = .25;
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
    private boolean m_isDemo = true;
    /**
    *
    */
    private DoubleSupplier m_turnMultipier;

    public DriveTrain(DoubleSupplier turnMultipier) {

        m_turnMultipier = turnMultipier;

        SmartDashboard.putNumber("Demo Speed", 0.1);

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

        // Current Masters Limit
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            /**
            * Configure the current limits that will be used
            * Stator Current is the current that passes through the motor stators.
            *  Use stator current limits to limit rotor acceleration/heat production
            * Supply Current is the current that passes into the controller from the supply
            *  Use supply current limits to prevent breakers from tripping 
            *                                             enabled | Limit(amp) | Trigger Threshold(amp) | Trigger Threshold Time(s) */
            if(m_isDemo){
                //m_talonsMaster[talonIndex].configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 40, 50, 1.0));
                m_talonsMaster[talonIndex].configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 15, 0.5));
            }else{
                //m_talonsMaster[talonIndex].configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 40, 50, 1.0));
                m_talonsMaster[talonIndex].configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 30, 45, 0.5));
            }
        }

        // Current Followers Limit
        for (talonIndex = 0; talonIndex < kMaxNumberOfFollowerMotors; talonIndex++) {
            /* enabled | Limit(amp) | Trigger Threshold(amp) | Trigger Threshold Time(s) */
            if(m_isDemo){
                m_talonsFollowers[talonIndex]
                        .configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 10, 25, 1.0));
                m_talonsFollowers[talonIndex]
                        .configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 5, 15, 0.5));
            }else{
                m_talonsFollowers[talonIndex]
                    .configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 20, 25, 1.0));
                m_talonsFollowers[talonIndex]
                    .configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(true, 10, 15, 0.5));
            }
        }

        // set all the Talon feedback Devices
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
            m_talonsMaster[talonIndex].configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor,
                    Constants.kPIDLoopIdx, Constants.kTimeoutMs);
        }

        // set all Talon SRX encoder values to zero
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talonsMaster[talonIndex].setPosition(0);
            m_talonsMaster[talonIndex].setSelectedSensorPosition(0, 0, Constants.kTimeoutMs);
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

        // put all Talon masters into brake mode
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            m_talonsMaster[talonIndex].setNeutralMode(NeutralMode.Coast);

        }

        // put all Talon Followers into brake mode
        for (talonIndex = 0; talonIndex < kMaxNumberOfFollowerMotors; talonIndex++) {
            m_talonsFollowers[talonIndex].setNeutralMode(NeutralMode.Coast);

        }

        // ensure ramp rate set accordingly
        if (m_useVoltageRamp) {
            for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
                // m_talonsMaster[talonIndex].setVoltageRampRate(m_voltageRampRate);
                m_talonsMaster[talonIndex].configClosedloopRamp(0, Constants.kTimeoutMs);
                //if(m_isDemo){
                 //   m_talonsMaster[talonIndex].configClosedloopRamp(m_closedLoopRamp_Demo_sec, Constants.kTimeoutMs);
                //}else{
                  //  m_talonsMaster[talonIndex].configClosedloopRamp(m_closedLoopRamp_sec, Constants.kTimeoutMs);
                //}
            }
        } else {
            // clear all voltage ramp rates
            for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
                m_talonsMaster[talonIndex].configOpenloopRamp(0);
            }
        }

        rightTalonFollower1.follow(rightTalonMaster);
        rightTalonFollower2.follow(rightTalonMaster);
        leftTalonFollower1.follow(leftTalonMaster);
        leftTalonFollower2.follow(leftTalonMaster);

        // Also need to set up the "inverted motors" array for the mecanum drive
        // code
        m_invertedMotors[kLeft] = -1;
        m_invertedMotors[kRight] = 1;

        setWheelPIDF();
        init();
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

        if(m_isDemo){
            my_shiftLow();
        }else{
            my_shiftHigh();
        }
        
    }
    /**
     * Set the Robot Demo Mode
     * 
     * @param isDemo
     */
    public void setRobotDemoMode(boolean isDemo){
        m_isDemo = isDemo;
    }

    /**
     * Returns true if Robot is in Demo Mode
     * @return
     */
    public boolean getRobotDemoMode(){
        return m_isDemo;
    }

    public void Toggle_Controlmode(){
        if(m_closedLoopTalonFXMode == TalonFXControlMode.Velocity){
            setOpenLoopMode();
        }else{
            setClosedLoopMode();
        }
    }

    public void setClosedLoopMode() {
        m_closedLoopTalonFXMode = TalonFXControlMode.Velocity;
        m_closedLoopMode = true;
        setWheelPIDF();
        DriverStation.reportError("Drive Train in Closed Loop Mode", false);
       
    }

    public void setOpenLoopMode() {
        m_closedLoopTalonFXMode = TalonFXControlMode.PercentOutput;
        m_closedLoopMode = false;
        DriverStation.reportError("Drive Train in Open Loop Mode", false);
    }

    public void my_shiftHigh() {
        DriverStation.reportWarning("Shift Highs Gear", false);
        dBL_Sol_Shifter.set(HIGHGEAR_VALUE);
        m_maxWheelSpeed_Current = m_maxWheelSpeed_HighGear;
        rightTalonMaster.selectProfileSlot(0, 0);
        leftTalonMaster.selectProfileSlot(0, 0);
        m_kPIDLoopIdx = 0;

        SmartDashboard.putBoolean("High Gear", true);
    }

    public void my_shiftLow() {
        DriverStation.reportWarning("Shift Low Gear", false);
        dBL_Sol_Shifter.set(LOWGEAR_VALUE);
        m_maxWheelSpeed_Current = m_maxWheelSpeed_LowGear;
        rightTalonMaster.selectProfileSlot(1, 0);
        leftTalonMaster.selectProfileSlot(1, 0);
        m_kPIDLoopIdx = 1;

        SmartDashboard.putBoolean("High Gear", false);
    }

    public void my_ShiftToggle(){
        if(my_GetIsCurrentGearHigh()){
            System.out.println("vvvvvvvvv  LOW vvvvvvvvvvvvv");
            my_shiftLow();
        }else{
            System.out.println("^^^^^^^^^^^  HIGH  ^^^^^^^^^^^^");
            my_shiftHigh();
        }
    }

    public void setWheelPIDF() {
        int talonIndex = 0;
        double wheelP_HighGear = 0.0;// 0.5;
        double wheelI_HighGear = 0.0;
        double wheelD_HighGear = 0.0;
        double wheelF_HighGear = 1023.0 / 20660.0;

        double wheelP_LowGear = 0.0;// 1.5;
        double wheelI_LowGear = 0.0;
        double wheelD_LowGear = 0.0;
        double wheelF_LowGear = 1023.0 / 20660.0;

        double wheelP_MotionMagic = 0.0;// 0.5;
        double wheelI_MotionMagic = 0.0;
        double wheelD_MotionMagic = 0.0;
        double wheelF_MotionMagic = 1023.0 / 20660.0;

        // set the PID values for each individual wheel
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            // m_talons[talonIndex].setPID(wheelP, wheelI, wheelD, wheelF, 0,
            // m_voltageRampRate, 0);
            m_talonsMaster[talonIndex].config_kP(0, wheelP_HighGear, 0);
            m_talonsMaster[talonIndex].config_kI(0, wheelI_HighGear, 0);
            m_talonsMaster[talonIndex].config_kD(0, wheelD_HighGear, 0);
            m_talonsMaster[talonIndex].config_kF(0, wheelF_HighGear, 0);
            // m_talonsMaster.config_IntegralZone(0, 30);

            m_talonsMaster[talonIndex].config_kP(1, wheelP_LowGear, 0);
            m_talonsMaster[talonIndex].config_kI(1, wheelI_LowGear, 0);
            m_talonsMaster[talonIndex].config_kD(1, wheelD_LowGear, 0);
            m_talonsMaster[talonIndex].config_kF(1, wheelF_LowGear, 0);
            // m_talonsMaster.config_IntegralZone(1, 30);

            m_talonsMaster[talonIndex].config_kP(2, wheelP_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kI(2, wheelI_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kD(2, wheelD_MotionMagic, 0);
            m_talonsMaster[talonIndex].config_kF(2, wheelF_MotionMagic, 0);
            // m_talonsMaster.config_IntegralZone(2, 30);
        }
        DriverStation.reportError("setWheelPIDF:\n", false);
    }

    @Override
    public void periodic() {
        // This method will be called once per scheduler run
        SmartDashboard.putNumber("Current Heading", getHeading());

    }

    @Override
    public void simulationPeriodic() {
        // This method will be called once per scheduler run when in simulation

    }

    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void my_toggle_preserveHeading(){
        if(m_preserveHeading_Enable){
            m_preserveHeading_Enable = false;
        }else{
            m_preserveHeading_Enable = true;
        }
    }


    public void my_DriveArcade(double xSpeed, double zRotation) {
        my_DriveArcade(xSpeed, zRotation, true);
    }

    public void my_DriveArcade(double xSpeed, double zRotation, boolean squareInputs) {

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

        xSpeed = MathUtil.clamp(xSpeed, -1.0, 1.0);
        xSpeed = applyDeadband(xSpeed, m_deadband);

        zRotation = MathUtil.clamp(zRotation, -1.0, 1.0);
        zRotation = applyDeadband(zRotation, m_deadband);

        // Square the inputs (while preserving the sign) to increase fine control
        // while permitting full power.
        if (squareInputs) {
            xSpeed = Math.copySign(xSpeed * xSpeed, xSpeed);
            zRotation = Math.copySign(zRotation * zRotation, zRotation);
        }

        // Apply Rotation Scaller

        if (my_GetIsCurrentGearHigh()) {

           
            zRotation = zRotation * .2;// TURN_FACTOR;

        } else { // In low gear

            zRotation = zRotation * 1;
        }


        // update count of iterations since rotation last commanded
        if ((-0.01 < zRotation) && (zRotation < 0.01)) {
            // rotation is practically zero, so just set it to zero and
            // increment iterations
            zRotation = 0.0;
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
                zRotation = -(m_desiredHeading - getHeading()) * kP_preserveHeading_Telepo;
                // SmartDashboard.putNumber("MaintainHeaading ROtation", rotation);
            }else{
                //This clears the iterations Counter so If m_preserveHeading_Enable is enables a new m_desiredHeading can be captureed
                m_iterationsSinceRotationCommanded = 0;
            }
        }

        if(m_isDemo){
            double demoSpeed = SmartDashboard.getNumber("Demo Speed", 0.1);
            xSpeed=xSpeed * demoSpeed;
            zRotation = zRotation * Utils.scale(m_turnMultipier.getAsDouble(), -1, 1, 0.1, 0.0);
        }

        driveCartesian(xSpeed, zRotation);
        // differentialDrive1.arcadeDrive(-xSpeed, zRotation);
    }

    /**
     * Returns 0.0 if the given value is within the specified range around zero. The
     * remaining range between the deadband and 1.0 is scaled from 0.0 to 1.0.
     *
     * @param value    value to clip
     * @param deadband range around zero
     */
    protected double applyDeadband(double value, double deadband) {
        if (Math.abs(value) > deadband) {
            if (value > 0.0) {
                return (value - deadband) / (1.0 - deadband);
            } else {
                return (value + deadband) / (1.0 - deadband);
            }
        } else {
            return 0.0;
        }
    }

    public boolean my_GetIsCurrentGearHigh() {
        if (dBL_Sol_Shifter.get() == HIGHGEAR_VALUE) {
            return true;
        } else {
            return false;
        }
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

    private void driveCartesian(double xSpeed, double zRotation) {
        int talonIndex = 0;

        //SmartDashboard.putNumber("xSpeed",xSpeed);
        //SmartDashboard.putNumber("xRotation", zRotation);

        m_wheelSpeeds[kLeft] = xSpeed + zRotation;
        m_wheelSpeeds[kRight] = xSpeed - zRotation;

        normalizeAndScaleWheelSpeeds();
        correctInvertedMotors();

        // want to do all the sets immediately after one another to minimize
        // delay between commands
        // set all Talon SRX encoder values to zero
        SmartDashboard.putNumber("Left talon", m_wheelSpeeds[kLeft]);
        SmartDashboard.putNumber("Right talon", m_wheelSpeeds[kRight]);
        //System.out.println(m_closedLoopTalonFXMode);
        for (talonIndex = 0; talonIndex < kMaxNumberOfMasterMotors; talonIndex++) {
            m_talonsMaster[talonIndex].set(m_closedLoopTalonFXMode, m_wheelSpeeds[talonIndex]);
            // m_talonsMaster[talonIndex].set(m_closedLoopTalonFXMode,
            // m_wheelSpeeds[talonIndex]);
        }
    }

    private void normalizeAndScaleWheelSpeeds() {
        int i;
        double tempMagnitude;
        double maxMagnitude;

        //SmartDashboard.putNumber("a_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
        //SmartDashboard.putNumber("a_wheelSpeeds[kRight]", m_wheelSpeeds[kRight]);
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
        //SmartDashboard.putNumber("b_wheelSpeeds[kLeft]", m_wheelSpeeds[kLeft]);
        //SmartDashboard.putNumber("b_wheelSpeeds[kRight]", m_wheelSpeeds[kRight]);
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
}
