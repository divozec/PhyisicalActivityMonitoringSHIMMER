package rad.diplomski.myapplication.tools;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import rad.diplomski.myapplication.Main.ExerciseFragment;
import rad.diplomski.myapplication.R;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by dzec0 on 24.8.2017..
 */

public class ActivityRecognizedService extends IntentService {

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
    }
}
    public boolean vehicle = false;
    public boolean bicycle = false;
    public boolean walking = false;
    public boolean running = false;
    public boolean still = false;
    ExerciseFragment ef = new ExerciseFragment();


    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
            for( DetectedActivity activity : probableActivities ) {
                switch( activity.getType() ) {
                    case DetectedActivity.IN_VEHICLE: {
                        Log.e( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 90) {
                            vehicle = true;
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Driving in a vehicle" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Driving in a vehicle");
//stuff that updates ui

                                }
                            });

                        }
                        break;
                    }
                    case DetectedActivity.ON_BICYCLE: {
                        Log.e( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 90 ) {
                            bicycle = true;
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Cycling" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Cycling");
//stuff that updates ui

                                }
                            });


                        }
                        break;
                    }
                    case DetectedActivity.ON_FOOT: {
                        Log.e( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.RUNNING: {
                        Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 90 ) {
                            running = true;
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Running" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Running");
//stuff that updates ui

                                }
                            });

                        }
                        break;
                    }
                    case DetectedActivity.STILL: {
                        Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );if( activity.getConfidence() >= 90 ) {
                            still = true;
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Still" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Still");
//stuff that updates ui

                                }
                            });

                        }
                        break;
                    }
                    case DetectedActivity.TILTING: {
                        Log.e( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                        break;
                    }
                    case DetectedActivity.WALKING: {
                        Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 90) {
                            walking = true;
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Walking" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Walking");
//stuff that updates ui

                                }
                            });

                        }
                        break;
                    }
                    case DetectedActivity.UNKNOWN: {
                        Log.e( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                        if( activity.getConfidence() >= 90) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Activity is unknown" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ef.activity.setText("Unknown");
//stuff that updates ui

                                }
                            });

                        }
                        break;
                    }
                }
            }
        }
    }

