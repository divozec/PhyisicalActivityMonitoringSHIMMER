package rad.diplomski.myapplication.FSM;

import rad.diplomski.myapplication.Main.ExerciseFragment;
import rad.diplomski.myapplication.R;
import rad.diplomski.myapplication.tools.User;

/**
 * Created by dzec0 on 15.7.2017..
 */

public class HighALL extends State {
    User user = new User();
    ExerciseFragment ef = new ExerciseFragment();

    public void HR_on(double hr) {

        //Jeli hr>0.9maxTHR (DA-USPORI)
        if(hr>0.9* user.maxTargetHeartRate()){
            ef.messages.setText(R.string.poruka_4);

        }// NE-3.Jeli korisnik fit
        else if(user.getAthlete()==1 ){

            if (ef.getSteps() > 2500) {
                ef.messages.setText(R.string.poruka_3);
            }
            ef.messages.setText(R.string.poruka_1);
        }
    }

    public void HR_off(double hr) {
        ef.messages.setText(R.string.poruka_16);
    }

    public void GSR_on(double gsr) {
        if(gsr < (ef.getGSR()*0.1)){
            if(user.getAge()>60){
                ef.messages.setText(R.string.poruka_8);
            }else if(user.getAthlete()==0)
            {
                ef.messages.setText(R.string.poruka_8);
            }
        }
        ef.messages.setText(R.string.poruka_6);
    }

    public void GSR_off(double gsr) {
        ef.messages.setText(R.string.poruka_16);
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
        ef.messages.setText(R.string.poruka_16);
    }
}
