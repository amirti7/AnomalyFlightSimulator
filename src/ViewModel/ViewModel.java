package ViewModel;

import Model.Model;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.stage.FileChooser;
import properties.Properties;
import test.AnomalyReport;
import test.Circle2D;
import test.CorrelatedFeatures;
import test.TimeSeries;

import java.io.File;

import java.util.*;

public class ViewModel extends Observable {

    public String csvName , pluginClassName;
    public int csvSize;
    public test.TimeSeries ts, ts2;
    public DoubleProperty aileron, throttle, elevators, rudder;
    public StringProperty gps, altitude, speed, direction, yaw, pitch, roll, rate;
    public IntegerProperty timeStep, seconds, minutes, hours;
    public Model model;
    public BooleanProperty stopClicked;
    public List<CorrelatedFeatures> correlatedFeaturesList = new ArrayList();
    public List<AnomalyReport> anomalyReportList = new ArrayList<>();
    public HashMap<String, Circle2D> circle = new HashMap<>();
    public List<Float>maxZscore=new ArrayList();
    public Properties properties=new Properties();
    public boolean XmlChosen=false;


    public ViewModel() {
        aileron = new SimpleDoubleProperty();
        elevators = new SimpleDoubleProperty();
        rudder = new SimpleDoubleProperty();
        throttle = new SimpleDoubleProperty();
        rate = new SimpleStringProperty();
        gps = new SimpleStringProperty();
        altitude = new SimpleStringProperty();
        speed = new SimpleStringProperty();
        direction = new SimpleStringProperty();
        yaw = new SimpleStringProperty();
        roll = new SimpleStringProperty();
        pitch = new SimpleStringProperty();
        timeStep = new SimpleIntegerProperty(1);
        seconds = new SimpleIntegerProperty(0);
        minutes = new SimpleIntegerProperty(0);
        hours = new SimpleIntegerProperty(0);
        rate = new SimpleStringProperty("1.0");
        model = new Model(timeStep);
        stopClicked = new SimpleBooleanProperty(false);


//        ts = new TimeSeries("reg_flight.csv");
//        ts.ReadFromFile("reg_flight.csv");
//
//        this.csvSize = ts.lines.size();


        timeStep.addListener((o, ov, nv) -> {

            if(!model.stopClicked) {
                Platform.runLater(() -> updateProperties(nv.intValue()));
            }


        });
    }

    public void play() {
        model.play();
    }

    public void pause() {
        model.pause();
    }

    public void stop() {
        model.stop();
    }

    public void setTimeSeries(TimeSeries ts){ model.setTimeSeries(ts); }

    public void openPlugin() {
        String filePath = "";
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Plug-in Class:");
        fc.setInitialDirectory(new File("./"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            this.pluginClassName= file.getName();
            this.pluginClassName=this.pluginClassName.substring(0,this.pluginClassName.length()-6);
            model.LoadPluginClass(file.getParent(), file.getName());
            this.correlatedFeaturesList=model.loadedClass.getCorrelatedList();
            this.anomalyReportList=model.loadedClass.getAnomalyList();
            this.circle=model.loadedClass.getCircle();
            this.maxZscore=model.loadedClass.getMaxZscore();

        }
    }


    public void openCsvfile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open CSV file:");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);
        if (chosen != null) {
            this.csvName = chosen.getAbsolutePath();
            ts2 = new TimeSeries(csvName);
            ts2.ReadFromFile(csvName);
            model.timeSeriesAnomaly = ts2;
            this.csvSize = ts.lines.size();
        }
    }

    public void openXmlSettings() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Settings file:");
        fc.setInitialDirectory(new File("./"));
        File chosen = fc.showOpenDialog(null);
        if (chosen != null) {
            properties.deserializeFromXML(chosen.getAbsolutePath());
            InitSettings();
            XmlChosen=true;
        }
    }

    public void InitSettings(){
        model.host= properties.getHost();
        model.port= properties.getPort();
        model.playSpeed=properties.getRate();
    }
    public void fixRateUp() {
            if (model.rate == 2)
                return;
            model.rate += 0.5;
            this.rate.set(model.rate.toString());
            if (model.timer != null) {
                model.pause();
                model.play();
            }
    }

    public void fixRateDown() {
            if (model.rate == 1)
                return;
            model.rate -= 0.5;
            this.rate.set(model.rate.toString());

            if (model.timer != null) {
                model.pause();
                model.play();
            }

    }

    public void updateProperties(int timeStep) {
        if (timeStep < ts.lines.size()) {
            aileron.set(Double.parseDouble(ts.getValueByTimeStep(0, timeStep)));
            elevators.set(Double.parseDouble(ts.getValueByTimeStep(1, timeStep)));
            rudder.set(Double.parseDouble(ts.getValueByTimeStep(2, timeStep)));
            throttle.set(Double.parseDouble(ts.getValueByTimeStep(6, timeStep)));
            altitude.set(ts.getValueByTimeStep(25, timeStep));
            speed.set(ts.getValueByTimeStep(24, timeStep));
            direction.set(ts.getValueByTimeStep(36, timeStep));
            yaw.set(ts.getValueByTimeStep(20, timeStep));
            roll.set(ts.getValueByTimeStep(28, timeStep));
            pitch.set(ts.getValueByTimeStep(29, timeStep));
            gps.set(ts.getValueByTimeStep(34, timeStep));
            seconds.setValue((timeStep % 60));
            minutes.setValue((timeStep / 60));
            hours.setValue((timeStep / 3600));
            this.timeStep.set(timeStep);
            setChanged();
            Platform.runLater(()->notifyObservers());
        }
    }


}
