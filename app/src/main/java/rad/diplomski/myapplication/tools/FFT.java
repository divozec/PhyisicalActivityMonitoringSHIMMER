package rad.diplomski.myapplication.tools;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 * Created by dzec0 on 15.6.2017..
 */

public class  FFT  {


    public double[] transform( double[] input)
    {

        double[] tempConversion = new double[input.length];

        FastFourierTransformer transformer = new FastFourierTransformer();
        try {
            Complex[] complx = transformer.transform(input);

            for (int i = 0; i < complx.length; i++) {
                double rr = (complx[i].getReal());
                double ri = (complx[i].getImaginary());

                tempConversion[i] = Math.sqrt((rr * rr) + (ri * ri));
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        return tempConversion;
    }
}


