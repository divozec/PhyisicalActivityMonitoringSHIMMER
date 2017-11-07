package rad.diplomski.myapplication.tools;


import android.widget.Toast;

import rad.diplomski.myapplication.Main.ExerciseSetup;

/**
 * Created by dzec0 on 15.6.2017..
 */

public class User {

    private int age;
    private double weight;
    private double height;
    private int gender = 1; // male 1 female 0
    private int athlete;
    private int obesity = 0;
    private int bodyType = 1;
    private double stepLength;


    //Age setter*
    public void setAge(String age) {
        this.age = Integer.parseInt(age);
    }

    //Age getter
    public int getAge() {
        return age;
    }


    //Weight setter
    public void setWeight(String weight) {
        this.weight = Double.parseDouble(weight);
    }

    //Weight getter
    public double getWeight() {
        return weight;
    }

    //Height setter
    public void setHeight(String height) {
        this.height = Double.parseDouble(height);
    }

    //Height getter
    public double getHeight() {
        return height;
    }

    //BMI = (Weight in Kilograms / (Height in centimeters x Height in centimeters))
    public double calculateBMI() {

        return ((weight / (height * height))) * 10000;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return gender;
    }


    //BF% = 1.20 x BMI + 0.23 x age - 10.8 x gender - 5.4
    public double calculateBFpercentage() {
        double bfp = 0;

        bfp = 1.20 * calculateBMI() + 0.23 * age - 10.8 * gender - 5.4;

//        if (bfp > 32 && gender == 0 || bfp > 25 && gender == 1) {
//            obesity = 1;
//        }

        return bfp;
    }

    public double calculateWaterConsumption() {
        double waterIntake = 0;
        if (age <= 30) {
            waterIntake = 0.04 * weight;
        } else if (age > 30 && age <= 54) {
            waterIntake = 0.03 * weight;
        } else {
            waterIntake = 0.03 * weight;
        }

        return waterIntake;
    }

    //Maximum Heart Rate - maximum number of beats your heart should beat ever
    //MHR Tanaka equation (best for obesity)
    public double maximumHeartRate() {

        return 208 - (0.7 * age);
    }

    //Still ok if user goes here but will be alarmed
    public double minTargetHeartRate() {

        return maximumHeartRate() * 0.5;
    }

    //Shouldn't go here. Stop activity immediately
    public double maxTargetHeartRate() {

        return maximumHeartRate() * 0.7;
    }

    //Set 1 if athletic
    public void setAthlete(int athlete) {
        this.athlete = athlete;
    }

    public int getAthlete() {
        return athlete;
    }

    public int bodyType() {
        if (athlete == 1)
            if (calculateBMI() < 17.5) {
                this.bodyType = 0;
            } else if (17.5 < calculateBMI() && calculateBMI() < 27) {
                this.bodyType = 1;
            } else {
                this.bodyType = 2;
            }
        else {
            if (calculateBMI() < 18.5) {
                this.bodyType = 0;
            } else if (18.5 < calculateBMI() && calculateBMI() < 25.5) {
                this.bodyType = 1;
            } else {
                this.bodyType = 2;
            }
        }
        return bodyType;
    }
    public double calculateStepLength() {
        if (gender == 1) {
            return height * 0.415;
        } else
            return height * 0.413;
    }
}

