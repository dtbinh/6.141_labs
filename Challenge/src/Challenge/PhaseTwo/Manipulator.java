package Challenge.PhaseTwo;

import java.util.ArrayList;

import Challenge.Waypoint;

import org.ros.message.rss_msgs.ArmMsg;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * <p>Manipulator Controller</p>
 *
 * @author dgonz
 *
 **/

public class Manipulator {

	Publisher<ArmMsg> armPublisher;
	
	//Some definitions and variables
	
	//Constants
	public final static double I2M=.0254;  //Inches to Meters
	public final double O_Z_A=5*I2M;  //Height from ground to Robot origin. To measure in m
	public final double A_Z_B=5.25*I2M;  //Height from Robot origin to Arm. To measure in m
	public final double A_X_B=2.5*I2M;  //Length from Robot origin to base of Arm. To measure in m
	public final double L_ARM=.245;  //Length of Arm. To measure in m
	public final double L_WRIST=.13;  //Length of Wrist. to measure in m
	public final static double Y_MAX=.505;  //Max Height
	public final double Y_MIN=.015;  //Min Height
	
	public static final short HAND_PORT=2;  //Servo port for gripper
	public static final int HAND_BIGOPEN=2000;  //Servo value for gripper full open
	public static final int HAND_SMALLOPEN=1850;  //Servo value for gripper open but still with IR sensor working
	public static final int HAND_HOLD=800;  //Servo value for max gripping force
	
	public static final short WRIST_PORT=4;  //Servo port for wrist	
	public static final int WRIST_90=500;  //various exact angles
	public static final int WRIST_45=825;
	public static final int WRIST_0=1350;
	public static final int WRIST_MINUS45=1650;
	public static final int WRIST_MINUS90=2125;
	public static final double M_WRIST=-9.0556; //degrees to servo value slope
	public static final int B_WRIST=1290;  //degrees to servo value y-intercept
	
	public static final short ARM_PORT=5;  //Servo port for arm
	public static final int ARM_90=1960;  //various exact angles
	public static final int ARM_45=1600;
	public static final int ARM_0=1250;
	public static final int ARM_MINUS45=825;
	public static final double M_ARM=8.5111;  //degrees to servo value slope
	public static final int B_ARM=1211;  //degrees to servo value y-intercept
	
	
	//state variables
	double w=0; //Wrist Angle
	double a=0; //Arm Angle
	int h=0; //Hand State (PWM)
	
	public int armPWM=0;
	public int wristPWM=0;
	public int handPWM=0;
	
	public Manipulator(Node node){
		armPublisher = node.newPublisher("command/Arm", "rss_msgs/ArmMsg");
		while(!armPublisher.hasSubscribers()){
			
		}
		//goToPose(0,L_ARM+O_Z_A+A_Z_B,w,HAND_SMALLOPEN);
	}
	public void debugMe(){
		System.err.println("Manipulator Debug\n" +
				"degToServo(0,M_ARM,B_ARM): "+degToServo(0,M_ARM,B_ARM)+
				"\ndegToServo(20,M_ARM,B_ARM): "+degToServo(20,M_ARM,B_ARM)+
				"\ndegToServo(80,M_ARM,B_ARM): "+degToServo(80,M_ARM,B_ARM)+
				"\ndegToServo(-30,M_ARM,B_ARM): "+degToServo(-30,M_ARM,B_ARM)+
				"\ndegToServo(-45,M_ARM,B_ARM): "+degToServo(-45,M_ARM,B_ARM)+
				"\ndegToServo(0,M_WRIST,B_WRIST): "+degToServo(0,M_WRIST,B_WRIST)+
				"\ndegToServo(45,M_WRIST,B_WRIST): "+degToServo(45,M_WRIST,B_WRIST)+
				"\ndegToServo(-45,M_WRIST,B_WRIST): "+degToServo(-45,M_WRIST,B_WRIST)+
				"\nMath.asin((.2-O_Z_A-A_Z_B)/L_ARM)="+Math.asin((.2-O_Z_A-A_Z_B)/L_ARM)+
				"\nMath.asin((.4-O_Z_A-A_Z_B)/L_ARM)="+Math.asin((.4-O_Z_A-A_Z_B)/L_ARM)+
				"\nMath.asin((Y_MIN-O_Z_A-A_Z_B)/L_ARM)="+Math.asin((Y_MIN-O_Z_A-A_Z_B)/L_ARM)+
				"\nMath.asin((Y_MAX-O_Z_A-A_Z_B)/L_ARM)="+Math.asin((Y_MAX-O_Z_A-A_Z_B)/L_ARM));
	}
	
	public void servoOut(short port,int value){
		
		ArmMsg publishMsg = new ArmMsg();
		
		if (port==(short)5){
			publishMsg.pwms = new long[] {value,degToServo(w,M_WRIST,B_WRIST),h,0,0,0,0,0};
			armPWM=value;
		}else if(port==(short)4){
			publishMsg.pwms = new long[] {degToServo(a,M_ARM,B_ARM),value,h,0,0,0,0,0};
			wristPWM=value;
		}else if(port==(short)2){
			publishMsg.pwms = new long[] {degToServo(a,M_ARM,B_ARM),degToServo(w,M_WRIST,B_WRIST),value,0,0,0,0,0};
			handPWM=value;
		}
		
		//publish the message made
		this.armPublisher.publish(publishMsg);
	}
	
	public void servoOut2(int wristVal,int armVal,int handVal){
		armPWM=armVal;
		wristPWM=wristVal;
		handPWM=handVal;
		ArmMsg publishMsg = new ArmMsg();
		publishMsg.pwms = new long[] {(long)armVal,(long)wristVal,(long)handVal,0,0,0,0,0};
		//publish the message made
		this.armPublisher.publish(publishMsg);
	}
	
	public void closeGripper(){
		servoOut2(wristPWM,armPWM,HAND_HOLD);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void openGripper(){
		servoOut2(wristPWM,armPWM,HAND_BIGOPEN);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int degToServo(double theta, double m, int b){
		return (int) (b+theta*m);
	}
	
	public void goToPickUp(){
		goToY(Y_MIN);
		servoOut(HAND_PORT,HAND_BIGOPEN);
		h=HAND_BIGOPEN;
	}
	public void goToPickUp2(){
		servoOut2(600,500,HAND_BIGOPEN);
	}
	
	public void goToY(double y){
		double aOld=a;
		double wOld=w;
		a=Math.asin((y-O_Z_A-A_Z_B)/L_ARM)*180/Math.PI;
		w=-a;
		/*for (double i=0;i<=1;i+=.01){ 
			servoOut(ARM_PORT,degToServo(aOld-(aOld-a)*i,M_ARM,B_ARM));
			servoOut(WRIST_PORT,degToServo(wOld-(wOld-w)*i,M_WRIST,B_WRIST));
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		servoOut(ARM_PORT,degToServo(a,M_ARM,B_ARM));
		servoOut(WRIST_PORT,degToServo(w,M_WRIST,B_WRIST));
		}*/

		servoOut(ARM_PORT,degToServo(a,M_ARM,B_ARM));
		servoOut(WRIST_PORT,degToServo(w,M_WRIST,B_WRIST));
	}

	
	public void goToPose(double x, double y, double t, int handPWM){
		double tOld=t;
		goToY(y);
		servoOut(HAND_PORT,handPWM);
	/*	for (double i=0;i<=1;i+=.01){
			servoOut(WRIST_PORT,degToServo(tOld-(tOld-t)*i,M_WRIST,B_WRIST));
			
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		

		servoOut(WRIST_PORT,degToServo(t,M_WRIST,B_WRIST));
	}
	
}
