package rad.diplomski.myapplication.FSM;

import android.util.Log;

/**
 * Created by dzec0 on 15.7.2017..
 */

public class Normal extends State {

    HighHR hHR = new HighHR();
    HighEMG hEMG = new HighEMG();
    HighGSR hGSR = new HighGSR();


    public void HR_on(double hr) {
        hHR.HR_on(hr);
    }

    public void HR_off(double hr) {
        Log.d("Normal", "HR_off");
    }

    public void GSR_on(double gsr) {
        hGSR.GSR_on(gsr);
    }
    public void GSR_off(double gsr) {
        Log.d("Normal", "GSR_off");
    }
    public void EMG_on(double emg) {
        hEMG.EMG_on(emg);
    }
    public void EMG_off(double emg) {
        Log.d("Normal", "EMG_off");
    }
}
