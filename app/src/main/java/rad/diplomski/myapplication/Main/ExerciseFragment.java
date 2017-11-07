package rad.diplomski.myapplication.Main;

import android.app.PendingIntent;
import android.os.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.Configuration;
import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.driver.ShimmerVerDetails;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import rad.diplomski.myapplication.FSM.FSM;
import rad.diplomski.myapplication.Pedometer.StepDetector;
import rad.diplomski.myapplication.Pedometer.StepListener;
import rad.diplomski.myapplication.R;
import rad.diplomski.myapplication.database.DatabaseHandler;
import rad.diplomski.myapplication.database.ShimmerConfiguration;
import rad.diplomski.myapplication.service.MultiShimmerTemplateService;
import rad.diplomski.myapplication.tools.ActivityRecognizedService;
import rad.diplomski.myapplication.tools.FFT;
import rad.diplomski.myapplication.tools.MeanAndMedian;
import rad.diplomski.myapplication.tools.User;

import static android.content.Context.SENSOR_SERVICE;
import static android.view.View.VISIBLE;


/**
 * Created by dzec0 on 18.6.2017..
 */

public class ExerciseFragment extends Fragment implements SensorEventListener, StepListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Varijable
    private static double METRIC_WALKING_FACTOR = 0.708;
    public static TextView tvHeartRate;
    public static TextView tvGSR;
    public static TextView tvEMG;
    public static TextView tv_steps;
    public static TextView messages;
    public static TextView calories;
    public static TextView activity;
    static List<Double> listGSR = new ArrayList<Double>();
    static List<Double> listEMG = new ArrayList<Double>();

    static User user = new User();
    static private String[] mBluetoothAddressforConn = new String[7];
    static private String[][] mSensorsforConn = new String[7][Shimmer.MAX_NUMBER_OF_SIGNALS];
    public HashMap<String, List<SelectedSensors>> mSelectedSensors = new HashMap<String, List<SelectedSensors>>(7);
    boolean[][] mSelectedSignals;
    public static int heartRateCont;
    public static boolean enableHeartRate;
    DatabaseHandler db;
    String[] deviceNames;
    String[] deviceBluetoothAddresses;
    String[][] mEnabledSensorNames;
    int numberofChilds[];
    boolean firstTime = true;
    View rootView;
    MultiShimmerTemplateService mService;
    public static String mBluetoothAddressToHeartRate;
    public static int heartRateRefresh;
    //Steps sensor
    boolean running;
    Chronometer chronometer;
    long time = 0;
    int i = 0;
    Button chronopause;
    Button chronostart;
    static FFT fft = new FFT();
    static FSM fsm = new FSM();
    static MeanAndMedian mam = new MeanAndMedian();
    public StepDetector simpleStepDetector;
    public SensorManager sensorManager;
    public Sensor accel;
    public GoogleApiClient mApiClient;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private double mCalories;
    private int age;
    private double height;
    private double weight;
    private double stepLength;
    int rCals;
    NumberFormat formatter = new DecimalFormat("#0.00");
    static double emg0;
    double gsr0;
    static int bpm;


    // The empty constructor
    public ExerciseFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().invalidateOptionsMenu();
        sensorManager = (SensorManager) ExerciseFragment.this.getActivity().getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        Log.d("Vrijednosti", "Weight= " + user.getWeight() + "Height= " + user.getHeight());
        //--Activity recognition API
        mApiClient = new GoogleApiClient.Builder(ExerciseFragment.this.getActivity())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
        //--
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            age = bundle.getInt("Age");
            weight = bundle.getDouble("Weight");
            height = bundle.getDouble("Height");
            stepLength = bundle.getDouble("StepL");
        }
        Log.d("Vrijednosti 2", "Vrijednosti su =" + age + " " + weight + " " + height + " " + stepLength);
        rootView = inflater.inflate(R.layout.exercise, container, false);

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


        chronostart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chronometer.setBase(SystemClock.elapsedRealtime() + time);
                chronometer.start();
                chronopause.setVisibility(VISIBLE);
            }
        });
        chronopause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                time = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();
            }
        });
        return rootView;
    }

    public void updateShimmerConfigurationList(List<ShimmerConfiguration> shimmerConfigurationList) {

        //save configuration settings
        db.saveShimmerConfigurations("Temp", mService.mShimmerConfigurationList);

        //query service get the deviceNames and Bluetooth addresses which are streaming
        shimmerConfigurationList = mService.getStreamingDevices();
        deviceNames = new String[shimmerConfigurationList.size()];
        deviceBluetoothAddresses = new String[shimmerConfigurationList.size()];
        mEnabledSensorNames = new String[shimmerConfigurationList.size()][Shimmer.MAX_NUMBER_OF_SIGNALS];
        numberofChilds = new int[shimmerConfigurationList.size()];
        mSelectedSignals = mService.getPlotSelectedSignals();


        int pos = 0;
        for (ShimmerConfiguration sc : shimmerConfigurationList) {
            deviceNames[pos] = sc.getDeviceName();
            deviceBluetoothAddresses[pos] = sc.getBluetoothAddress();
            Shimmer shimmer = mService.getShimmer(deviceBluetoothAddresses[pos]);
            mEnabledSensorNames[pos] = shimmer.getListofEnabledSensorSignals();
            numberofChilds[pos] = getNumberofChildren(sc.getEnabledSensors(), sc.getBluetoothAddress());
            ArrayList<SelectedSensors> sensors = new ArrayList<SelectedSensors>();
            if (mSelectedSignals != null)
                for (int i = 0; i < shimmer.getListofEnabledSensorSignals().length; i++)
                    sensors.add(new SelectedSensors(mEnabledSensorNames[pos][i], mSelectedSignals[pos][i]));
            else
                for (int i = 0; i < shimmer.getListofEnabledSensorSignals().length; i++)
                    sensors.add(new SelectedSensors(mEnabledSensorNames[pos][i], false));

            mSelectedSensors.put(deviceBluetoothAddresses[pos], sensors);
            pos++;
        }

    }

    public int getNumberofChildren(long enabledSensors, String bluetoothAddress) {
        int count = 1; //timestamp
        int shimmerVersion = mService.getShimmerVersion(bluetoothAddress);
        if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3 || shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_SR30) {
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_ACCEL) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFFFF) & Shimmer.SENSOR_DACCEL) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_GYRO) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_MAG) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFFFF) & Shimmer.SENSOR_BATT) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_EXT_ADC_A15) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_EXT_ADC_A7) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_EXT_ADC_A6) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_INT_ADC_A1) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_INT_ADC_A12) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_INT_ADC_A13) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_INT_ADC_A14) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFFFFFF) & Shimmer.SENSOR_GSR) > 0) {
                count = count + 1;
            }
            if ((enabledSensors & Shimmer.SENSOR_BMP180) > 0) {
                count = count + 2;
            }
            if ((enabledSensors & 0x10) > 0) {
                count = count + 3;
            }
            if ((enabledSensors & 0x08) > 0) {
                count = count + 3;
            }
            if ((enabledSensors & 0x080000) > 0) {
                count = count + 3;
            }
            if ((enabledSensors & 0x100000) > 0) {
                count = count + 3;
            }
            if ((((enabledSensors & 0xFF) & Shimmer.SENSOR_ACCEL) > 0 || (((enabledSensors & 0xFFFF) & Shimmer.SENSOR_DACCEL) > 0)) && ((enabledSensors & 0xFF) & Shimmer.SENSOR_GYRO) > 0 && ((enabledSensors & 0xFF) & Shimmer.SENSOR_MAG) > 0 && mService.is3DOrientationEnabled(bluetoothAddress)) {
                count = count + 8; //axis angle and quartenion
            }
        } else {
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_ACCEL) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_GYRO) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_MAG) > 0) {
                count = count + 3;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_GSR) > 0) {
                count = count + 1;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_ECG) > 0) {
                count = count + 2;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EMG) > 0) {
                count++;
            }
            if (((enabledSensors & 0xFF00) & Shimmer.SENSOR_BRIDGE_AMP) > 0) { //because there is strain gauge high and low add twice
                count++;
                count++;
            }
            if (((enabledSensors & 0xFF00) & Shimmer.SENSOR_HEART) > 0) {
                count++;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EXP_BOARD_A0) > 0) {
                count++;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_EXP_BOARD_A7) > 0) {
                count++;
            }
            if (((enabledSensors & 0xFF) & Shimmer.SENSOR_ACCEL) > 0 && ((enabledSensors & 0xFF) & Shimmer.SENSOR_GYRO) > 0 && ((enabledSensors & 0xFF) & Shimmer.SENSOR_MAG) > 0 && mService.is3DOrientationEnabled(bluetoothAddress)) {
                count = count + 8; //axis angle and quartenion
            }
        }

        return count;

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
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("rad.diplomski.myapplication.service.MultiShimmerTemplateService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Shimmer.MESSAGE_READ:
                    if ((msg.obj instanceof ObjectCluster)) {
                        ObjectCluster objectCluster = (ObjectCluster) msg.obj;
                        if (enableHeartRate) {
                            if (objectCluster.mBluetoothAddress.equals(mBluetoothAddressToHeartRate)) {
                                if (heartRateCont == heartRateRefresh) {
                                    Collection<FormatCluster> temp = objectCluster.mPropertyCluster.get("Heart Rate");
                                    FormatCluster formatClustertemp = (FormatCluster) ObjectCluster.returnFormatCluster(temp, "CAL");
                                    if (formatClustertemp != null) {

                                        bpm = (int) formatClustertemp.mData;
                                        tvHeartRate.setText(String.valueOf(bpm));
                                        int check = checkHR(bpm);
                                        if (check == 0) {
                                            fsm.HR_off(bpm);
                                        } else if (check == 1) {
                                            fsm.HR_on(bpm);
                                        }

                                    }
                                    heartRateCont = 0;
                                } else
                                    heartRateCont++;
                            }
                        }

                        Collection<FormatCluster> dataFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer3.ObjectClusterSensorName.GSR);  // first retrieve all the possible formats for the current sensor device
                        FormatCluster formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(dataFormats, "CAL")); // retrieve the calibrated data
                        if (formatCluster != null) {

                            final double gsr = formatCluster.mData;
                            tvGSR.setText(String.valueOf(gsr));
                            if (listGSR.size() < 100) {
                                listGSR.add(gsr);
                            }
                            double gsr0 = calculateAverage(listGSR);
                            int check = checkGSR(gsr, gsr0);
                            if (check == 0) {
                                fsm.GSR_off(gsr);
                            } else if (check == 1) {
                                fsm.GSR_on(gsr);
                            }

                        }


                        dataFormats = objectCluster.mPropertyCluster.get(Configuration.Shimmer3.ObjectClusterSensorName.EMG_CH2_24BIT);  // first retrieve all the possible formats for the current sensor device
                        formatCluster = ((FormatCluster) ObjectCluster.returnFormatCluster(dataFormats, "CAL")); // retrieve the calibrated data
                        if (formatCluster != null) {

                            final double emg = (int) formatCluster.mData;
                            tvEMG.setText(String.valueOf(emg));
                            if (listEMG.size() < 1024) {
                                listEMG.add(emg);
                            }
                            double[] processedEMGarr = fftEMG();
                            emg0 = mam.median(processedEMGarr);
                            int check = checkEMG(emg, emg0);
                            if (check == 0) {
                                fsm.EMG_off(emg);
                            } else if (check == 1) {
                                fsm.EMG_on(emg);
                            }

                        }

                        break;
                    }
            }
        }
    };

    private static double calculateAverage(List<Double> l) {
        double sum = 0;
        for (Double d : l)
            sum += d;
        return (sum / l.size());
    }

    private static double[] fftEMG() {
        double[] arrayEMG = new double[listEMG.size()];
        for (int i = 0; i < listEMG.size(); i++) {
            arrayEMG[i] = listEMG.get(i);
        }
        Arrays.sort(arrayEMG);
        return fft.transform(arrayEMG);
    }

    private static int checkEMG(double emg, double emg0) {
        int counter = 0;

        if (emg < emg0 * 0.5) {
            counter++;
            if (counter > 150)
                return 1;
        }
        return 0;
    }


    private static int checkGSR(double gsr, double gsr0) {

        if (gsr < gsr0 * 0.3) {
            return 1;
        }

        return 0;
    }

    private static int checkHR(int bpm) {
        if (bpm < user.minTargetHeartRate()) {
            return 0;
        } else if (bpm > user.minTargetHeartRate()) {
            return 1;
        } else if (bpm > user.maxTargetHeartRate()) {
            return 1;
        }
        return 0;
    }


    public void onResume() {
        super.onResume();
        firstTime = true;

        this.mService = ((MainActivity) getActivity()).mService;
        if (mService != null) {
            setup();
        }
        enableHeartRate = mService.isHeartRateEnabled() || mService.isHeartRateEnabledECG();

        if (enableHeartRate) {

            mBluetoothAddressToHeartRate = mService.mBluetoothAddressToHeartRate;
            heartRateRefresh = (int) mService.getSamplingRate(mBluetoothAddressToHeartRate);
        }


    }


    public void setup() {


        db = mService.mDataBase;
        mService.mShimmerConfigurationList = db.getShimmerConfigurations("Temp");
        if (firstTime) {
            updateShimmerConfigurationList(mService.mShimmerConfigurationList);
            firstTime = false;
        }
        mService.setGraphHandler(mHandler, "");
        mService.enableGraphingHandler(true);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported on device.", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, -1);
            }
        }
        tv_steps = (TextView) rootView.findViewById(R.id.tv_steps);
        tvHeartRate = (TextView) rootView.findViewById(R.id.tvHR);
        tvGSR = (TextView) rootView.findViewById(R.id.tvGSR);
        tvEMG = (TextView) rootView.findViewById(R.id.tvEMG);
        activity = (TextView) rootView.findViewById(R.id.tvActivity);
        chronometer = (Chronometer) rootView.findViewById(R.id.chronometer);
        chronopause = (Button) rootView.findViewById(R.id.chronopause);
        chronostart = (Button) rootView.findViewById(R.id.chronostart);
        messages = (TextView) rootView.findViewById(R.id.tvMessages);
        calories = (TextView) rootView.findViewById(R.id.tvCalories);

        numSteps = 0;
        sensorManager.registerListener(ExerciseFragment.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }


    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(ExerciseFragment.this);
        running = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(ExerciseFragment.this);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;

        mCalories += weight * METRIC_WALKING_FACTOR * stepLength / 100000;
        rCals = (int) mCalories;
        tv_steps.setText(Integer.toString(numSteps));
        calories.setText(Integer.toString(rCals));
    }

    public double getGSR() {
        return gsr0;
    }

    public double getHR() {
        return bpm;
    }

    public double getEMG() {
        return emg0;
    }

    public double getSteps() {
        return numSteps;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(ExerciseFragment.this.getActivity(), ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(ExerciseFragment.this.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 5000, pendingIntent);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
