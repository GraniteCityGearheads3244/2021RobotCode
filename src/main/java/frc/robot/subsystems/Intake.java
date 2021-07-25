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

import frc.robot.commands.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

/**
 *
 */
public class Intake extends SubsystemBase {

    private DoubleSolenoid doubleSolenoid1;
    private WPI_VictorSPX motor;

    private Compressor compressor;
    /**
    *
    */
    public Intake() {


        compressor = new Compressor(0);
        //compressor.stop();

        doubleSolenoid1 = new DoubleSolenoid(0, 3, 2);
        addChild("Double Solenoid 1", doubleSolenoid1);

        motor = new WPI_VictorSPX(6);

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
    public void my_Extend() {
        doubleSolenoid1.set(DoubleSolenoid.Value.kForward);
    }

    public void my_Retract() {
        DriverStation.reportWarning("my_Retract()", false);
        // DriverStation.reportWarning(DoubleSolenoid.Value.kReverse, false);
        doubleSolenoid1.set(Value.kReverse);
    }

    public void my_rollerRUN(double speed) {
        motor.set(speed);
    }

}
