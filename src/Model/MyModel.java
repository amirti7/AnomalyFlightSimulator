package Model;


import test.TimeSeries;

public interface MyModel {

void setTimeSeries(TimeSeries ts);

void play();

void pause();

void stop();



}
