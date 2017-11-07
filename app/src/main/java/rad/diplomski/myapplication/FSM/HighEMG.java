package rad.diplomski.myapplication.FSM;

import rad.diplomski.myapplication.Main.ExerciseFragment;
import rad.diplomski.myapplication.R;
import rad.diplomski.myapplication.tools.User;

/**
 * Created by dzec0 on 15.7.2017..
 */

public class HighEMG extends State {

    ExerciseFragment ef = new ExerciseFragment();
    User user = new User();
    int counter = 0;

    public void HR_on(double hr) {
        ef.messages.setText(R.string.poruka_18);
    }

    public void HR_off(double hr) {

    }

    public void GSR_on(double gsr) {
        ef.messages.setText(R.string.poruka_17);
    }

    public void GSR_off(double gsr) {
    }

    public void EMG_on(double emg) {

        if (user.getAthlete() == 0 && user.getAge() > 60) {
            {
                ef.messages.setText(R.string.poruka_14);
            }
            if(user.getAge()<60){
                ef.messages.setText(R.string.poruka_13);
            }
        }

            ef.messages.setText(R.string.poruka_10);

    }

    public void EMG_off(double emg) {
        ef.messages.setText(R.string.poruka_1);
    }
}
