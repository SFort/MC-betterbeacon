package tf.ssf.sfort.betterbeacon;

public interface BeaconAccessor{
    void addRange(double d);
    void resetRange();
    double getRange();
}