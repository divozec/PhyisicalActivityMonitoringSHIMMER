package rad.diplomski.myapplication.Main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rad.diplomski.myapplication.database.DatabaseHandler;
import rad.diplomski.myapplication.service.MultiShimmerTemplateService;
import rad.diplomski.myapplication.R;
import rad.diplomski.myapplication.tools.InputFilterMinMax;
import rad.diplomski.myapplication.tools.User;

import static android.view.View.VISIBLE;


public class ExerciseSetup extends Fragment implements View.OnClickListener{

    //Varijable

    private EditText etAge;
    private EditText etHeight;
    private EditText etWeight;
    private TextView tvBMI;
    private TextView tvWaterConsumption;
    private RadioGroup rgSex;
    private RadioGroup rgBodyType;
    private Button btnConfirmParameters;
    private Button btnStartExercising;
    private Button btnShimmerSetup;
    private InputFilter inputFilter;
    private RadioButton male;
    private RadioButton female;
    private RadioButton normal;
    private RadioButton athletic;

    double bmi;
    double waterIntake;
    double bfpercentage;
    double maxHR;
    double maxTHR;
    double minTHR;
    double stepLength;

    User user = new User();
    static private String[] mBluetoothAddressforConn = new String[7];
    static private String[][] mSensorsforConn = new String[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
    public HashMap<String, List<SelectedSensors>> mSelectedSensors = new HashMap<String, List<SelectedSensors>>(7);
    boolean[][] mSelectedSignals;
    DatabaseHandler db;
    String[] deviceNames;
    String[] deviceBluetoothAddresses;
    String[][] mEnabledSensorNames;
    int numberofChilds[];
    boolean firstTime = true;
    View rootView;
    MultiShimmerTemplateService mService;


    public ExerciseSetup() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.exercise_setup, container, false);
        ;
        Arrays.fill(mBluetoothAddressforConn, "");
        for (String[] row : mSensorsforConn) {
            Arrays.fill(row, "");
        }

        for (String[] row : mSensorsforConn) {
            Arrays.fill(row, "RAW");
        }

        firstTime = true;

        this.mService = ((MainActivity) getActivity()).mService;

        if (mService != null) {
            setup();
        }

        btnConfirmParameters.setOnClickListener(this);
        tvBMI.setOnClickListener(this);
        tvWaterConsumption.setOnClickListener(this);
        btnStartExercising.setOnClickListener(this);
        btnShimmerSetup.setOnClickListener(this);
        return rootView;
    }

    public void ToastBmi() {
        switch (user.getAthlete()) {
            case 1:
                if (bmi < 17.5) {
                    Toast.makeText(ExerciseSetup.this.getContext(), "You are underweight. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();
                } else if (17.5 < bmi && bmi < 27) {
                    Toast.makeText(ExerciseSetup.this.getContext(), "Your weight is normal. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ExerciseSetup.this.getContext(), "You are overweight. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();

                }
                break;
            case 0:
                if (bmi < 18.5) {
                    Toast.makeText(ExerciseSetup.this.getContext(), "You are underweight. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();
                } else if (18.5 < bmi && bmi < 25.5) {
                    Toast.makeText(ExerciseSetup.this.getContext(), "Your weight is normal. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ExerciseSetup.this.getContext(), "You are overweight. App will monitor your exercise accordingly", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
    public void ToastWater() {
        switch (user.getAthlete()) {
            case 1:
                Toast.makeText(ExerciseSetup.this.getContext(), "You should drink " + String.format( "%.1f Liters", waterIntake + 1 ) + " Liters of water per day.",Toast.LENGTH_LONG).show();
                break;
            case 0:
                Toast.makeText(ExerciseSetup.this.getContext(), "You should drink " + String.format( "%.1f Liters", waterIntake ) + " Liters of water per day.",Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void walkingSetup(String age, String weight, String height) {
        user.setAge(age);
        user.setWeight(weight);
        user.setHeight(height);
        rgSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbMale:
                        user.setGender(1);
                        break;
                    case R.id.rbFemale:
                        user.setGender(0);
                        break;
                }
            }
        });
        rgBodyType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbAthletic:
                        user.setAthlete(1);
                        break;
                    case R.id.rbNormal:
                        user.setAthlete(0);
                        break;
                }
            }
        });
        bmi = user.calculateBMI();
        waterIntake = user.calculateWaterConsumption();
        bfpercentage = user.calculateBFpercentage();
        maxHR = user.maximumHeartRate();
        minTHR = user.minTargetHeartRate();
        maxTHR = user.maxTargetHeartRate();
        stepLength = user.calculateStepLength();
    }



            public void onAttach(Activity activity) {
                super.onAttach(activity);
                Log.d("Activity Name", activity.getClass().getSimpleName());
                if (!isMyServiceRunning()) {
                    Intent intent = new Intent(getActivity(), MultiShimmerTemplateService.class);
                    getActivity().startService(intent);
                }
            }


            protected boolean isMyServiceRunning() {
                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if ("rad.diplomski.myapplication.service.MultiShimmerTemplateService".equals(service.service.getClassName())) {
                        return true;
                    }
                }
                return false;
            }





            public void setup() {

                etAge = (EditText) rootView.findViewById(R.id.etAge);
                etWeight = (EditText) rootView.findViewById(R.id.etWeight);
                etHeight = (EditText) rootView.findViewById(R.id.etHeight);
                btnConfirmParameters = (Button) rootView.findViewById(R.id.btnConfirmParams);
                btnStartExercising = (Button) rootView.findViewById(R.id.btnStartExercising);
                btnShimmerSetup = (Button) rootView.findViewById(R.id.btnShimmerSetup);
                tvWaterConsumption = (TextView) rootView.findViewById(R.id.tvWaterConsumption);
                tvBMI = (TextView) rootView.findViewById(R.id.tvBMI);
                rgSex = (RadioGroup) rootView.findViewById(R.id.rgSex);
                rgBodyType = (RadioGroup) rootView.findViewById(R.id.rgBodyType);
                male = (RadioButton) rootView.findViewById(R.id.rbMale);
                female = (RadioButton) rootView.findViewById(R.id.rbFemale);
                athletic = (RadioButton) rootView.findViewById(R.id.rbAthletic);
                normal = (RadioButton) rootView.findViewById(R.id.rbNormal);

                etAge.setFilters(new InputFilter[]{new InputFilterMinMax("1", "100")});
                etWeight.setFilters(new InputFilter[]{new InputFilterMinMax("1", "200")});
                etHeight.setFilters(new InputFilter[]{new InputFilterMinMax("1", "250")});


                db = mService.mDataBase;


            }


            public void onPause() {
                super.onPause();

            }

            @Override
            public void onStop() {
                super.onStop();

            }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.btnShimmerSetup:
                fragment = new DevicesFragment();
                replaceFragment(fragment);
                break;

            case R.id.btnStartExercising:
                fragment = new ExerciseFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Age", user.getAge());
                bundle.putDouble("Height", user.getHeight());
                bundle.putDouble("Weight", user.getWeight());
                bundle.putDouble("StepL", stepLength);
                fragment.setArguments(bundle);
                replaceFragment(fragment);
                break;

            case R.id.btnConfirmParams:
                String age = etAge.getText().toString();
                String weight = etWeight.getText().toString();
                String height = etHeight.getText().toString();

                if (age.isEmpty() || weight.isEmpty() || height.isEmpty()) {

                    AlertDialog alertDialog = new AlertDialog.Builder(ExerciseSetup.this.getActivity()).create();
                    alertDialog.setTitle("Missing parameters");
                    alertDialog.setMessage("Please enter all parameters in order to start your exercise!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }else {
                    walkingSetup(age, weight, height);
                }
                tvBMI.setText(String.format( "%.1f", bmi ));
                tvWaterConsumption.setText(String.format( "%.1f Liters", waterIntake ));
                btnStartExercising.setVisibility(VISIBLE);
                btnShimmerSetup.setVisibility(VISIBLE);
                Log.d("Vrijednosti",  "Weight= " + user.getWeight()+ "Height= " + user.getHeight());
                break;

            case R.id.tvBMI:
                ToastBmi();
                break;

            case R.id.tvWaterConsumption:
                ToastWater();
                break;

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Age", user.getAge());
        outState.putDouble("Height", user.getHeight());
        outState.putDouble("Weight", user.getWeight());
        outState.putDouble("Steps", stepLength);
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(((ViewGroup)getView().getParent()).getId(), someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
