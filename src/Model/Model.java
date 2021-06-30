package Model;

import javafx.beans.property.*;
import properties.Properties;
import test.TimeSeries;
import test.TimeSeriesAnomalyDetector;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Model extends Observable implements MyModel {

    public Timer timer =null;
    public boolean stopClicked=false;
    public TimeSeries timeSeries,timeSeriesAnomaly;
    public Properties myProperties;
    public IntegerProperty timeStep;
    public Double rate;
    public TimeSeriesAnomalyDetector loadedClass;
    private long fixedRate;
    private boolean openSocket=false;
    private PrintWriter out2fg;
    private Socket socket;
    public int port=5400;
    public String host="127.0.0.1";
    public int playSpeed;

    public Model(IntegerProperty timeStep) {
        myProperties=new Properties();
        this.timeStep=timeStep;
        openSocket2fg();
        rate=1.0;
    }

public void openSocket2fg(){
    try {
        socket=new Socket(host, port);
        out2fg=new PrintWriter(socket.getOutputStream());
        openSocket=true;
    } catch (IOException e) {
        return;
    }
}

//view model calls this function whenever view changes algorithm
public void LoadPluginClass(String FilePath, String ClassName){

    ClassLoader cl = new ClassLoader(FilePath, ClassName);
    this.loadedClass = cl.get();
    this.loadedClass.learnNormal(this.timeSeries);
    this.loadedClass.detect(this.timeSeriesAnomaly);
}


    private void flightGear(String fg){
        try {
            if(openSocket) {
                out2fg.println(fg);
                out2fg.flush();
            }
            else return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getLine(int lineNum) {
        return timeSeriesAnomaly.lines.get(lineNum);
    }

    @Override
    public void setTimeSeries(TimeSeries ts) {
        this.timeSeries=ts;
    }

    @Override
    public void play(){
        if(timer==null){
            timer=new Timer();

            fixedRate=(long)(playSpeed*(1/rate));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(timeStep.get()==timeSeriesAnomaly.lines.size()-1)
                        timer.cancel();
                    List<String> line = getLine(timeStep.get());
                    String toFg=String.join(",",line);
                    flightGear(toFg);
                    timeStep.set(timeStep.get()+1);
                }
            },0,fixedRate);
        }
    }

    public void pause(){
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
    }

    public void stop(){
        stopClicked=true;
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
        timeStep.set(1);
        stopClicked=false;
    }

    @Override
    public void finalize() {
        try{
            out2fg.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
