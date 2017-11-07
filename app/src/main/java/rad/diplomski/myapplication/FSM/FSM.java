package rad.diplomski.myapplication.FSM;

/**
 * Created by dzec0 on 15.7.2017..
 */

public class FSM {

    private State[] states = {new Normal(), new HighHR(), new HighHR_EMG(), new HighGSR(),
            new HighEMG(), new HighALL(), new HighEMG_GSR(), new HighHR_GSR()};

    private int[][] transition = {{1, 3, 4, 0, 0, 0},
            {1, 7, 2, 0, 1, 1},
            {2, 5, 2, 4, 2, 1},
            {7, 3, 6, 3, 0, 3},
            {2, 6, 4, 4, 4, 0},
            {5, 5, 5, 6, 2, 7},
            {5, 6, 6, 6, 4, 3},
            {7, 7, 5, 3, 1, 7}};

    private int current = 0;

    public void next(int msg) {
        current = transition[current][msg];
    }

    public void HR_on(double hr) {
        states[current].HR_on(hr);
        next(0);
    }

    public void HR_off(double hr) {
        states[current].HR_off(hr);
        next(3);
    }

    public void GSR_on(double gsr) {
        states[current].GSR_on(gsr);
        next(1);
    }
    public void GSR_off(double gsr) {
        states[current].GSR_off(gsr);
        next(4);
    }
    public void EMG_on(double emg) {
        states[current].EMG_on(emg);
        next(2);
    }
    public void EMG_off(double emg) {
        states[current]. EMG_off(emg);
        next(5);
    }

}
