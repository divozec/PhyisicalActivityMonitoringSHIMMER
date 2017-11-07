package rad.diplomski.myapplication.tools;

/**
 * Created by dzec0 on 15.6.2017..
 */

public class MeanAndMedian {

    public MeanAndMedian(){
    }

    public static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }
    // the array double[] m MUST BE SORTED
    public static double median(double[] m) {
        int middle = m.length/2;
        if (m.length%2 == 1) {
            return m[middle];
        } else {
            return (m[middle-1] + m[middle]) / 2.0;
        }
    }
}
