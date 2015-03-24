import javafx.geometry.Point2D;

//#4
public class Packet53ОФ350 extends Packet{
	public final double RADIUS = 0.038; //полкалибра снаряда
	public final double S = RADIUS * RADIUS * Math.PI; //площадь сечения шара
	public final double WEIGHT = 6.2;

	//характеристика среды
	public final double Cf = 1.905; //коэффициент для вычисления сопротивления воздуха

	public Packet53ОФ350(Double speed) {
		super(speed);
		mWindSpeed = new Point2D(0.0, 0.0);
	}

	@Override
	protected void calcAcceleration(){
		calcAirResistance();
		mAcceleration = mGravity.add(mAirForce).multiply(1.0/WEIGHT);
		System.out.println(WEIGHT);
	}

	@Override
	protected Point2D resistance(Double thickness, Point2D speed){
		return speed.normalize().multiply(Cf*thickness*speed.magnitude()*speed.magnitude()/2*S);
	}
}
