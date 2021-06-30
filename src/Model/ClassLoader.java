package Model;


import test.TimeSeriesAnomalyDetector;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class ClassLoader {

    public Class<?> c ;
    public String className;
    private TimeSeriesAnomalyDetector ad;

    public  ClassLoader(String filepath, String className) {

        this.className="test."+className;
        this.className=this.className.substring(0,this.className.length()-6);

        Class<?> AnomalyDetector = null;


        // load class directory
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = URLClassLoader.newInstance(new URL[]{
                    new URL("file://" + filepath)
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            this.c = urlClassLoader.loadClass(this.className);
            try {
                this.ad = (TimeSeriesAnomalyDetector) c.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TimeSeriesAnomalyDetector get(){
        return this.ad;
    }
//    @Override
//    public void learnNormal(TimeSeries timeSeries) {
//
//        this.ad.learnNormal(timeSeries);
//    }
//
//    //need to add anomaloy csv also;
//    @Override
//    public List<AnomalyReport> detect(TimeSeries var1) {
//
//        this.ad.detect(var1);
//
//        return ad.getAnomalyList();
//
//    }
//
//    @Override
//    public List<AnomalyReport> getAnomalyList() {
//        return null;
//    }
//
//    @Override
//    public List<CorrelatedFeatures> getCorrelatedList() {
//        return null;
//    }


}
