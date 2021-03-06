package Chassis;

//import MotorControlSolution.*;
import MotorControl.*;

/**
 * <p>A whole-robot position controller.</p>
 **/
public class RobotPositionController {
  // we like having these constants available.
  /**
   * <p>The maximum pwm command magnitude.</p>
   **/
  protected static final double MAX_PWM = WheelVelocityController.MAX_PWM;

  /**
   * <p>Student Code: unloaded maximum wheel angular velocity in rad/s.
   * This should be a protected static final double called MAX_ANGULAR_VELOCITY.</p>
   **/
  protected static final double MAX_ANGULAR_VELOCITY = WheelVelocityController.MAX_ANGULAR_VELOCITY;

  /**
   * <p>Student Code: radius of the wheel on the motor (in meters).
   * This should be a protected static final double called WHEEL_RADIUS_IN_M.</p>
   **/
  protected static final double WHEEL_RADIUS_IN_M = WheelVelocityController.WHEEL_RADIUS_IN_M;

  /**
   * <p>Student Code: encoder resolution.</p>
   * This should be a protected static final double called ENCODER_RESOLUTION.</p>
   **/
  protected static final double ENCODER_RESOLUTION = WheelVelocityController.ENCODER_RESOLUTION;

  /**
   * <p>Student Code: motor revolutions per wheel revolution.
   * This should be a protected static final double called GEAR_RATIO.</p>
   **/
  protected static final double GEAR_RATIO = WheelVelocityController.GEAR_RATIO;

  /**
   * <p>Student Code: encoder ticks per motor revolution.</p>
   * This should be a protected static final double called TICKS_PER_REVOLUTION.</p>
   **/
  protected static final double TICKS_PER_REVOLUTION = WheelVelocityController.TICKS_PER_REVOLUTION;

  /**
   * <p>Student Code: encoder ticks per meter.</p>
   * ticks/rev * rev/rad * rad/m = ticks/m
   **/
  protected static final double TICKS_PER_METER = TICKS_PER_REVOLUTION * (1 / (2 * Math.PI)) * (1 / WHEEL_RADIUS_IN_M);

  /**
   * <p>The whole-robot velocity controller.</p>
   **/
  // changed to Balanced so the right methods get called
  protected RobotVelocityControllerBalanced robotVelocityController;

  /**
   * <p>Total ticks since reset, positive means corresp side of robot moved
   * forward.</p>
   **/
  protected double[] totalTicks = new double[2];

  /**
   * <p>Total elapsed time since reset in seconds.</p>
   **/
  protected double totalTime = 0.0;

  /**
   * <p>Time in seconds since last update.</p>
   **/
  protected double sampleTime;

  /**
   * <p>An abstract gain; meaning depends on the particular subclass
   * implementation.</p>
   **/
  protected double gain = 1.0;

  // a handy thing to have
  protected final static double DISTANCE_BETWEEN_WHEELS = 0.430;

  /**
   * <p>The robot.</p>
   **/
  protected OdometryRobot robot;

  /**
   * <p>position state variables.</p>
   **/
  protected double x = 0;
  protected double y = 0;
  protected double theta = 0;

  /**
   * <p>Create a new position controller for a robot.</p>
   *
   * @param robot the robot, not null
   **/
  public RobotPositionController(OdometryRobot robot) {
    this.robot = robot;
  }

  /**
   * <p>Translate at the specified speed for the specified distance.</p>
   *
   * <p>Blocks until the motion is complete or errored.</p>
   *
   * @param speed the desired robot motion speed in m/s
   * @param distance the desired distance to move in meters, relative to the
   * robot's pose at time of call.
   *
   * @return true iff motion was successful
   **/
  public boolean translate(double speed, double distance) {
	boolean ok = true;
	// Begin Student Code

	//translation, Feed-Forward implementation

	double[] desiredPose={x+distance*Math.cos(theta),y+distance*Math.sin(theta),theta};
	double[] startPose={x,y,theta};
	double[] myPose={x,y,theta};

	if (distance < 0) {
		speed *= -1;
	}

	double angularVelocityDesired=speed/WHEEL_RADIUS_IN_M;

	double currentDistance = poseDistance(myPose, startPose);

	double aDist = 0.15; // acceleration distance
	double dDist = 0.15; // deceleration distance
	double currentAngVel = 0;
	double minAngVelStart = 0.01 / WHEEL_RADIUS_IN_M;
	double minAngVelEnd = 0.005 / WHEEL_RADIUS_IN_M;

	//Set angular velocity to 0
	robotVelocityController.setDesiredAngularVelocity(0,0);

	System.out.println("AVD: " + angularVelocityDesired);
	while (currentDistance < Math.abs(distance)){
		myPose[0]=x;
		myPose[1]=y;
		myPose[2]=theta;
		currentDistance = poseDistance(myPose, startPose);

		if (currentDistance < Math.abs(distance) * 0.5) {
			if (currentDistance < aDist) {
				currentAngVel = (currentDistance / aDist) * angularVelocityDesired;
			} else {
				currentAngVel = angularVelocityDesired;
			}
			if (speed > 0) {
				if (minAngVelStart > currentAngVel) {
					currentAngVel = minAngVelStart;
				}
			} else {
				if (minAngVelStart > Math.abs(currentAngVel)) {
					currentAngVel = -1 * minAngVelStart;
				}
			}
		} else {
			if (currentDistance > Math.abs(distance) - dDist) {
				currentAngVel = ((Math.abs(distance) - currentDistance) / dDist) * angularVelocityDesired;
			} else {
				currentAngVel = angularVelocityDesired;
			}
			if (speed > 0) {
				if (minAngVelEnd > currentAngVel) {
					currentAngVel = minAngVelEnd;
				}
			} else {
				if (minAngVelStart > Math.abs(currentAngVel)) {
					currentAngVel = -1 * minAngVelEnd;
				}
			}
		}
		robotVelocityController.setDesiredAngularVelocity(currentAngVel,currentAngVel);
	}

	//Set angular velocity to 0
	robotVelocityController.setDesiredAngularVelocity(0,0);

	printPose();
	System.out.println("desiredPose: x: "+desiredPose[0]+" y:"+desiredPose[1]+" theta: "+desiredPose[2]);
	System.out.println(comparePose(myPose, desiredPose, 0.1, 0.15));
	System.out.println("We got there.");

	if (!comparePose(myPose, desiredPose, 0.1, 0.10)) {
		return false;
	}

	// End Student Code
	return ok;
  }

  /**
   * <p>Rotate at the specified speed for the specified angle.</p>
   *
   * <p>Blocks until the motion is complete or errored.</p>
   *
   * @param speed the desired robot motion speed in radians/s
   * @param angle the desired angle to rotate in radians, relative to the
   * robot's pose at time of call.
   *
   * @return true iff motion was successful
   **/
  public boolean rotate(double speed, double angle) {
	boolean ok = true;
	// Begin Student Code
	
	boolean leftOrRight;

	double angularVelocityDesired = (speed * DISTANCE_BETWEEN_WHEELS / 2) / WHEEL_RADIUS_IN_M;

	double targetAngle = theta + angle;
	double[] desiredPose={x,y,targetAngle};
	double[] startPose={x,y,theta};
	double[] myPose={x,y,theta};

	System.out.println("ANGLE: " + angle);
	if (angle > 0) {
		System.out.println("LEFT");
		leftOrRight = true;
		robotVelocityController.setDesiredAngularVelocity(-1 * angularVelocityDesired,angularVelocityDesired);
	} else {
		System.out.println("RIGHT");
		leftOrRight = false;
		robotVelocityController.setDesiredAngularVelocity(angularVelocityDesired,-1 * angularVelocityDesired);
	}

	while (!comparePose(myPose, desiredPose, 0.1, 0.01) && angularVelocityDesired > (0.05 * DISTANCE_BETWEEN_WHEELS / 2) / WHEEL_RADIUS_IN_M) {
		myPose[0] = x;
		myPose[1] = y;
		myPose[2] = theta;
		
		if (angle < 0 && theta < targetAngle) {
			if (!leftOrRight) {
				angularVelocityDesired *= 0.5;
				leftOrRight = true;
			}
			robotVelocityController.setDesiredAngularVelocity(-1 * angularVelocityDesired,angularVelocityDesired);
		} else if (angle < 0 && theta > targetAngle) {
			if (leftOrRight) {
				angularVelocityDesired *= 0.5;
				leftOrRight = false;
			}
			robotVelocityController.setDesiredAngularVelocity(angularVelocityDesired,-1 * angularVelocityDesired);
		} else if (angle > 0 && theta > targetAngle) {
			if (leftOrRight) {
				angularVelocityDesired *= 0.5;
				leftOrRight = false;
			}
			robotVelocityController.setDesiredAngularVelocity(angularVelocityDesired,-1 * angularVelocityDesired);
		} else if (angle > 0 && theta < targetAngle) {
			if (!leftOrRight) {
				angularVelocityDesired *= 0.5;
				leftOrRight = true;
			}
			robotVelocityController.setDesiredAngularVelocity(-1 * angularVelocityDesired,angularVelocityDesired);
		}
	}

	//Set angular velocity to 0
	robotVelocityController.setDesiredAngularVelocity(0,0);

	printPose();
	System.out.println("desiredPose: x: "+desiredPose[0]+" y:"+desiredPose[1]+" theta: "+desiredPose[2]);
	System.out.println(comparePose(myPose, desiredPose, 0.1, 0.15));
	System.out.println("We got there.");

	if (!comparePose(myPose, desiredPose, 0.1, 0.10)) {
		return false;
	}

	// End Student Code
	return ok;
  }
    
  public double rerangeAngle(double a) {
	while (a >= 2 * Math.PI) {
		a -= 2 * Math.PI;
	}
	while (a < 0) {
		a += 2 * Math.PI;
	}
	return a;
  }

    

  /**
   * <p>If position control is closed-loop, this computes the new left and
   * right velocity commands and issues them to {@link
   * #robotVelocityController}.</p>
   **/
  public synchronized void controlStep() {

    if (robotVelocityController == null)
      return;

    if (!robot.motorsEnabled() || robot.estopped())
      return;

    // Begin Student Code (if implementing closed-loop control)
    // End Student Code (if implementing closed-loop control)
  }

  /**
   * <p>Set the whole-robot velocity controller.</p>
   *
   * <p>This is called automatically by {@link OdometeryRobot}.</p>
   *
   * @param vc the whole-robot velocity controller
   **/
  public void setRobotVelocityController(RobotVelocityController vc) {
    // the cast was made necessary by the type change for robotVelocityController, documented above
    robotVelocityController = (RobotVelocityControllerBalanced)vc;
  }

  /**
   * <p>Set {@link #gain}.</p>
   *
   * @param g the new gain
   **/
  public void setGain(double g) {
    gain = g;
  }

  /**
   * <p>Get {@link #gain}.</p>
   *
   * @return gain
   **/
  public double getGain() {
    return gain;
  }

  /**
   * <p>prints the current pose.</p>
   **/
  public void printPose(){
	  System.out.println("Current Pose: X: "+x+" Y: "+y+" Theta: "+theta);
  }

  /**
   * <p>Compute the distance between two poses.
   *
   * Ignore angle.
   *
   * @param pose1 first pose
   * @param pose2 second pose
   **/

  public double poseDistance(double [] pose1, double [] pose2){
	return Math.pow(Math.pow(pose1[0] - pose2[0], 2) + Math.pow(pose1[1] - pose2[1], 2), 0.5);
  }

  /**
   * <p>Check if pose1 is near pose 2 within certain tolerances</p>
   *
   * @param pose1 pose to check
   * @param pose2 pose to compare to
   * @param toleranceLinear linear tolerance in meters
   * @param toleranceAngular angular tolerance in radians
   **/

  public boolean comparePose(double [] pose1, double [] pose2, double toleranceLinear, double toleranceAngular){
  	double a1 = pose1[2];
  	double a2 = pose2[2];

	//keep a1 in range
	a1 = rerangeAngle(a1);

	//keep a2 in range
	a2 = rerangeAngle(a2);

	return (poseDistance(pose1, pose2) < toleranceLinear) && (Math.abs(a1 - a2) < toleranceAngular);
  }

  /**
   * <p>Update feedback and sample time.</p>
   *
   * @param time the time in seconds since the last update, saved to {@link
   * #sampleTime}
   * @param leftTicks left encoder ticks since last update, positive means
   * corresp side of robot rolled forward
   * @param rightTicks right encoder ticks since last update, positive means
   * corresp side of robot robot rolled forward
   **/
  public synchronized void update(double time,
                                  double leftTicks, double rightTicks) {

    sampleTime = time;

    totalTicks[RobotBase.LEFT] += leftTicks;
    totalTicks[RobotBase.RIGHT] += rightTicks;
    totalTime += time;

    //Convert from ticks to meters
    double rightDist = rightTicks / TICKS_PER_METER;
    double leftDist = leftTicks / TICKS_PER_METER;

    //Useful definitions
    double distDiff = rightDist - leftDist;
    double distAvg = (rightDist + leftDist) / 2;
    double dtheta = distDiff / DISTANCE_BETWEEN_WHEELS;
    double thetaTravel = theta + dtheta / 2;

    //update our current odometry
    double thetaNew = theta + dtheta;
    double xNew = x + Math.cos(thetaTravel) * distAvg;
    double yNew = y + Math.sin(thetaTravel) * distAvg;

    //keep thetaNew in range
    //thetaNew = rerangeAngle(thetaNew);

    //apply the new odometry
    x = xNew;
    y = yNew;
    theta = thetaNew;
  }
}
