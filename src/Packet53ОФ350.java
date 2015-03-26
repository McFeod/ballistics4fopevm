import javafx.geometry.Point2D;

//#4
public class Packet53ОФ350 extends Packet{
	public final double RADIUS = 0.038; //полкалибра снаряда
	public final double S = RADIUS * RADIUS * Math.PI; //площадь сечения шара
	public final double WEIGHT = 6.2;

	//характеристика среды
	public final double Cf = 0.905; //коэффициент для вычисления сопротивления воздуха

	public Packet53ОФ350(Double speed) {
		super(speed);
		mWindSpeed = new Point2D(0.0, 0.0);
		mGravity = new Point2D(0.0, -WEIGHT*G);
	}

	@Override
	protected void calcAcceleration(){
		calcAirResistance();
		mAcceleration = mGravity.add(mAirForce).multiply(1.0/WEIGHT);
		//System.out.println(WEIGHT);
	}

	protected double siacci(double V){
		double linearTerm = 0.2002 * V - 48.05;
		double middleTerm = Math.sqrt(
				Math.pow((0.1648*V - 47.95),2)
						+9.6);
		double squareTerm = 0.0442*V *(V-300) /
				(Math.pow((V / 200), 10)
						+ 371);
		return linearTerm + middleTerm + squareTerm;
	}


	@Override
	protected Point2D resistance(Double thickness, Point2D speed){
		double V = speed.magnitude();
		double siacci = siacci(V);
		double normal = Cf*thickness*V*V/2*S;
		return speed.normalize().multiply(RADIUS * RADIUS * 1000 * siacci);
	}
}
