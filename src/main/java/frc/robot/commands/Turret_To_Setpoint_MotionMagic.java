// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TurretAim_MM;

public class Turret_To_Setpoint_MotionMagic extends CommandBase {
  private final TurretAim_MM m_TurretAim_MM;
  private final DoubleSupplier m_setpoint;

  /** Creates a new Turrent_To_Setpoint_MotionMagic. */
  public Turret_To_Setpoint_MotionMagic(TurretAim_MM subsystem, DoubleSupplier setpoint) {

    m_TurretAim_MM = subsystem;
    addRequirements(m_TurretAim_MM);
    m_setpoint = setpoint;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_TurretAim_MM.my_Aim_MotoionMagic(m_setpoint.getAsDouble());
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_TurretAim_MM.my_Aim_PercentOutput(0.0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
