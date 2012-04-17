/*
 * 
 */
package Grasping;

import org.ros.message.MessageListener;
import org.ros.message.rss_msgs.ArmMsg;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

public class Grasping implements NodeMain{

	public static double[] targetAngles = {0.0,0.0,0.0};

	
	private Subscriber<org.ros.message.rss_msgs.ArmMsg> armSub;

	JointController jc;

	public static void setServoAngles(double[] desiredAngles){
		targetAngles = new double[] {desiredAngles[0],desiredAngles[1],desiredAngles[2]};
	}

	public static double[] inverseKinematics(double x2, double z2) {
		// Please excuse the variable names. For an explanation, see:
		// https://courses.csail.mit.edu/rss/wiki/index.php?title=Team6Lab72012#Arm_control_and_inverse_kinematics

		double x0 = .07; // rough estimate, TODO: replace with measurements!!!!
		double z0 = .12; // rough estimate, TODO: replace with measurements!!!!
		Mat p0 = encodePoint2(x0, z0);
		Mat p2 = encodePoint2(x2, z2);

		double l1 = .25; // rough estimate, TODO: replace with measurements!!!!
		double l2 = .15; // rough estimate, TODO: replace with measurements!!!!

		double phi = Math.atan2(z2 - z0, x2 - x0);
		double d = Mat.l2(Mat.add(p2, Mat.mul(-1, p0)));

		double x1_ = (d*d - l2*l2 + l1*l1) / (2 * d);
		double z1_ = Math.pow(4 * d*d * l1*l1 - Math.pow(d*d - l2*l2 + l1*l1, 2), 0.5) / (2 * d);

		double phi_ = Math.atan2(z1_, x1_);

		double theta1 = phi + phi_;
		double theta2 = Math.acos((x2 - x0 - l1 * Math.cos(theta1)) / l2) - theta1;

		return new double[]{theta1, theta2};
	}

	public static Mat encodePoint2(double x, double y) {
		Mat p = new Mat(2, 1);
		p.data[0][0] = x;
		p.data[1][0] = y;
		return p
	}

	public static double[] decodePoint2(Mat p) {
		double x = p.data[0][0];
		double y = p.data[1][0];
		return new double[]{x, y};
	}

	public class ArmListener implements MessageListener<ArmMsg> {

		@Override public void onNewMessage(ArmMsg currentMessage) {
			double currentShoulderAngle = ShoulderController.getAngleEquivalent(currentMessage.pwms[0]);
			double currentWristAngle = WristController.getAngleEquivalent(currentMessage.pwms[1]);
			double currentGripperAngle = GripperController.getAngleEquivalent(currentMessage.pwms[2]);
			
			if((currentShoulderAngle != targetAngles[0]) || (currentWristAngle != targetAngles[1])
					|| (currentGripperAngle != targetAngles[2])){
				
				jc.commandServos(currentMessage, targetAngles);
			}
		}
	}


	@Override
	public void onShutdown(Node arg0) {
		// TODO Auto-generated method stub

	}




	@Override
	public void onShutdownComplete(Node arg0) {
		// TODO Auto-generated method stub

	}




	@Override
	public void onStart(Node node) {
		jc = new JointController(node);

		armSub = node.newSubscriber("rss/ArmStatus", "rss_msgs/ArmMsg");
		armSub.addMessageListener(new ArmListener());

		setServoAngles(new double[]{Math.PI/2, Math.PI/2, Math.PI/2});
	}




	@Override
	public GraphName getDefaultNodeName() {
		// TODO Auto-generated method stub
		return null;
	}
}
