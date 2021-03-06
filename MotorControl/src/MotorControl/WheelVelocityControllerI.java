package MotorControl;

/**
 * <p>Closed-loop integral wheel velocity controller.</p>
 *
 * @author vona
 * @author prentice
 **/
public class WheelVelocityControllerI extends WheelVelocityController {

  /**
   * <p>The result of the previous control step.</p>
   **/
  protected double lastResult = 0;
  
  // Motor Control Lab, Part 8
  /**
   * <p>The integral of the error.</p>
   */
  protected double errorIntegral = 0;
  
  //protected static final double PROPROTIONAL_GAIN = MAX_PWM / MAX_ANGULAR_VELOCITY;
  protected static final double PROPORTIONAL_GAIN = 0.45 * 62;

  protected static final double INTEGRAL_GAIN = 1.2 * PROPORTIONAL_GAIN / 0.4;
  
  /**
   * {@inheritDoc}
   *
   * <p>This impl implements closed-loop integral control.</p>
   **/
  public double controlStep() {

    double result = 0;
    // Motor Control Lab, Part 8
    // Start Student Code
    double error = desiredAngularVelocity - currentAngularVelocity;
    errorIntegral += error*sampleTime;
    System.out.print("hist: ");
    System.out.println(sampleTime);
    System.out.print("hidav: ");
    System.out.println(desiredAngularVelocity);
    System.out.print("hicav: ");
    System.out.println(currentAngularVelocity);
    System.out.print("hiei: ");
    System.out.println(errorIntegral);
    result = PROPORTIONAL_GAIN*error + gain*INTEGRAL_GAIN*errorIntegral;
    // End Student Code

    if (result > MAX_PWM)
      result = MAX_PWM;

    if (result < -MAX_PWM)
      result = -MAX_PWM;

    lastResult = result;

    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This impl returns "I".</p>
   **/
  public String getName() {
    return "I";
  }
}
