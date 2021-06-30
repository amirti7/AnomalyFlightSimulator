package View;

import ViewModel.ViewModel;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.skins.ModernSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import properties.Properties;
import test.*;

import java.io.File;
import java.util.*;

public class Controller implements Observer {
     ViewModel viewModel;
    @FXML AnchorPane graph;
    @FXML AnchorPane joystickPane;
    @FXML Slider timeStep;
    @FXML Label rate;
    @FXML Slider throttleY;
    @FXML Slider rudderX;
    @FXML Canvas joystick;
    @FXML TextField Gps;
    @FXML TextField Altitude ;
    @FXML TextField Speed ;
    @FXML TextField Direction ;
    @FXML TextField Yaw ;
    @FXML TextField Roll ;
    @FXML TextField Pitch ;
    @FXML ListView<String> featuresList;
    @FXML Label hours;
    @FXML Label minutes;
    @FXML Label seconds;
    @FXML Button up;
    @FXML Button playButton;
    @FXML Button pauseButton;
    @FXML Button stopButton;
    @FXML Button rewindButton;
    @FXML Button fasFowardButton;
    @FXML Button down;
    @FXML LineChart<Number,Number> cf1;
    @FXML LineChart <Number,Number> cf2;
    @FXML LineChart<Number,Number> anomalyGraph;
    @FXML Canvas graphs;
    @FXML NumberAxis x1;
    @FXML NumberAxis y1;
    @FXML NumberAxis x;
    @FXML NumberAxis y;
    @FXML BubbleChart<Number,Number> welzlCircle;
    @FXML Gauge gps1;
    @FXML Gauge alt1;
    @FXML Gauge speed1;
    @FXML Gauge direction1;
    @FXML Gauge yaw1;
    @FXML Gauge roll1;
    @FXML Gauge pitch1;
    XYChart.Series<Number,Number> series=new XYChart.Series<Number,Number>();
    XYChart.Series<Number,Number> series2=new XYChart.Series<Number,Number>();
    XYChart.Series<Number,Number> anomalySeries = new XYChart.Series<>();
    XYChart.Series<Number,Number> redPointsSeries = new XYChart.Series<>();
    XYChart.Series welzlSeries=new XYChart.Series();
    XYChart.Series welzlAnomalySeries=new XYChart.Series();
    Alert plugInAlert = new Alert(Alert.AlertType.INFORMATION);
    Alert regFlightAlert = new Alert(Alert.AlertType.INFORMATION);
    HashMap<String,List<Long>> AnomalyReportHash=new HashMap<>();
    double currTimeStep;
    int algorithm=0;
    String Attribute="aileron";
    CorrelatedFeatures correlatedFeature;
    int plugInChoice;
    boolean pauseClicked=false;
    boolean checked=false;
    boolean playClicked=false;
    boolean clicked=false;
    boolean csvClicked=false;
    boolean LinePainted=false;
    boolean plugInSwitch=false;
    boolean regFlight=false;
    DoubleProperty aileron , elevators;
    double jx=0,jy=0,mx;


    void init(ViewModel vm){

        vm.addObserver(this);
        aileron=new SimpleDoubleProperty();
        elevators=new SimpleDoubleProperty();
        plugInAlert.setContentText("Cannot open PlugIn before Chosing a CSV file");
        plugInAlert.setTitle("Error");
        plugInAlert.setHeaderText("Error");
        regFlightAlert.setContentText("Must Choose Regular Flight CSV First!");
        regFlightAlert.setTitle("Error");
        regFlightAlert.setHeaderText("Error");
        cf1.setAnimated(false);
        cf2.setAnimated(false);
        anomalyGraph.setAnimated(false);
        this.viewModel=vm;
        welzlCircle.setAnimated(false);
        x.setForceZeroInRange(false);
        y.setForceZeroInRange(false);
        welzlCircle.setVisible(false);
        anomalyGraph.setVisible(true);
        welzlCircle.setTitle("Anomaly Detector Graph");
        //  view->viewModel

        //view <-> viewModel
        timeStep.valueProperty().bindBidirectional(vm.timeStep);

        //viewModel -> view
        aileron.bind(vm.aileron);
        elevators.bind(vm.elevators);
        throttleY.valueProperty().bind(vm.throttle);
        rudderX.valueProperty().bind(vm.rudder);
        Gps.textProperty().bind(vm.gps);
        Altitude.textProperty().bind(vm.altitude);
        Speed.textProperty().bind(vm.speed);
        Direction.textProperty().bind(vm.direction);
        Yaw.textProperty().bind(vm.yaw);
        Roll.textProperty().bind(vm.roll);
        Pitch.textProperty().bind(vm.pitch);
        seconds.textProperty().bind(vm.seconds.asString());
        minutes.textProperty().bind(vm.minutes.asString());
        hours.textProperty().bind(vm.hours.asString());
        rate.textProperty().bind(vm.rate);
        currTimeStep=1;


        Gps.setVisible(false);
        Altitude.setVisible(false);
        Speed.setVisible(false);
        Direction.setVisible(false);
        Yaw.setVisible(false);
        Roll.setVisible(false);
        Pitch.setVisible(false);

        throttleY.setAccessibleText("Throttle");
        throttleY.setMajorTickUnit(0.1);
        rudderX.setMajorTickUnit(0.1);
        throttleY.setShowTickLabels(true);
        rudderX.setShowTickLabels(true);
        initGauges();




    }


    public void openRegFlight(){

        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Regular Flight CSV:");
        fc.setInitialDirectory(new File("./"));
        File file = fc.showOpenDialog(null);
        if (file != null){
            viewModel.ts = new TimeSeries(file.getAbsolutePath());
            viewModel.ts.ReadFromFile(file.getAbsolutePath());
            viewModel.setTimeSeries(viewModel.ts);
            viewModel.csvSize = viewModel.ts.lines.size();
            regFlight=true;
        }
    }

    public void openXmlSettings(){
        viewModel.openXmlSettings();
        rudderX.setMin(viewModel.properties.getAttributes().get("rudder").getMin());
        rudderX.setMax(viewModel.properties.getAttributes().get("rudder").getMax());
        throttleY.setMax(viewModel.properties.getAttributes().get("throttle").getMax());
}

    public void setMouseEvent(){
        if(!clicked) {
            clicked = true;
            Attribute = featuresList.getSelectionModel().getSelectedItem();
        }
    }
    public void openCsvfile(){
        if(regFlight) {
            viewModel.openCsvfile();
            if(!viewModel.csvName.contains(".csv")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Wrong File Selected , Please Select a .csv file!");
                return;
            }
            csvClicked = true;
            this.showFeatures();
        }
        else{
            regFlightAlert.show();
        }
    }

    public void openPlugin(){
        if(csvClicked) {
            if (plugInSwitch) {
                pause();
                viewModel.openPlugin();
                setAnomalyHashMap();
                if (viewModel.pluginClassName.equals("SimpleAnomalyDetector"))
                    plugInChoice = 1;
                if (viewModel.pluginClassName.equals("ZscoreDetector"))
                    plugInChoice = 2;
                if (viewModel.pluginClassName.equals("HybridDetector"))
                    plugInChoice = 3;
                anomalyGraph.getData().clear();
                play();
            } else {
                viewModel.openPlugin();
                setAnomalyHashMap();
                if (viewModel.pluginClassName.equals("SimpleAnomalyDetector"))
                    plugInChoice = 1;
                if (viewModel.pluginClassName.equals("ZscoreDetector"))
                    plugInChoice = 2;
                if (viewModel.pluginClassName.equals("HybridDetector"))
                    plugInChoice = 3;
            }
        }
        else
            plugInAlert.show();

    }

    public void fastFoward(){
        viewModel.pause();
        if(checked) {
            if (timeStep.getValue() + 50 > viewModel.ts2.lines.size()){
                 viewModel.play();
                 return;
            }
            timeStep.setValue(timeStep.getValue() + 50);
        }
    }

    public void rewind(){
        viewModel.pause();
        if(checked) {
            if (timeStep.getValue() - 50 < 0){
                viewModel.play();
                return;
            }
            timeStep.setValue(timeStep.getValue() - 50);
        }
    }
    public void fixRateUp(){
        if(playClicked) {
            viewModel.fixRateUp();
        }
    }
    public void fixRateDown(){
        if(playClicked) {
            viewModel.fixRateDown();
        }
    }

    public void play(){
        if(csvClicked) {
            if(!playClicked) {
                if(!viewModel.XmlChosen) {
                    viewModel.properties.deserializeFromXML("./settings.xml");
                    viewModel.InitSettings();
                    rudderX.setMin(viewModel.properties.getAttributes().get("rudder").getMin());
                    rudderX.setMax(viewModel.properties.getAttributes().get("rudder").getMax());
                    throttleY.setMax(viewModel.properties.getAttributes().get("throttle").getMax());
                }
                pauseClicked=false;
                plugInSwitch=true;
                viewModel.play();
                playClicked = true;
                checked = true;
            }
        }
    }
    public void pause() {
        if (!pauseClicked) {
            if (playClicked) {
                viewModel.pause();
                playClicked = false;
            }
            pauseClicked=true;
        }
    }
    public void stop(){
        if (playClicked) {
            viewModel.stop();
            playClicked=false;
            pauseClicked=false;
        }
    }
    public void showFeatures(){
        for(int i=0;i<viewModel.ts2.lines.get(0).size();i++){
            featuresList.getItems().add(viewModel.ts2.lines.get(0).get(i));
        }
    }

    private void paintJoystick(){
        jx=aileron.getValue();
        jy= elevators.getValue();
        GraphicsContext gc = joystick.getGraphicsContext2D();
        mx=Math.pow(joystick.getWidth(),2)+Math.pow(joystick.getHeight(),2);
        mx=Math.pow(mx,0.5)/4;
        Double normalizeX = (jx/2)*joystick.getWidth();
        Double normalizeY = (jy/2)*joystick.getHeight();
        gc.clearRect(0,0, joystick.getWidth(), joystick.getHeight());
        gc.strokeOval(normalizeX+mx,normalizeY+mx,45,45);
        gc.fillOval(normalizeX+mx,normalizeY+mx,45,45);
        gc.setFill(Color.DIMGREY);
    }
    @Override
    public void update(Observable o, Object arg) {
//        pause();
        paintJoystick();

        switch (plugInChoice){
            case 1:
//                pause();
                welzlCircle.setVisible(false);
                anomalyGraph.setVisible(true);
                Simple();
//                play();
                break;


            case 2:
//                pause();
                welzlCircle.setVisible(false);
                anomalyGraph.setVisible(true);
                Zscore();
//                play();
                break;

            case 3:
                PaintHybridGraph();
                break;
        }


        gps1.setValue(Double.parseDouble(Gps.textProperty().getValue()));
        alt1.setValue(Double.parseDouble(Altitude.textProperty().getValue()));
        speed1.setValue(Double.parseDouble(Speed.textProperty().getValue()));
        direction1.setValue(Double.parseDouble(Direction.textProperty().getValue()));
        if(Double.parseDouble(Yaw.textProperty().getValue())<0)
        yaw1.setValue(Double.parseDouble(Yaw.textProperty().getValue())+360);
        else
            yaw1.setValue(Double.parseDouble(Yaw.textProperty().getValue()));
        if(Double.parseDouble(Roll.textProperty().getValue())<0)
            roll1.setValue(Double.parseDouble(Roll.textProperty().getValue())+360);
        else
            roll1.setValue(Double.parseDouble(Roll.textProperty().getValue()));
        if(Double.parseDouble(Pitch.textProperty().getValue())<0)
            pitch1.setValue(Double.parseDouble(Pitch.textProperty().getValue())+360);
        else
            pitch1.setValue(Double.parseDouble(Pitch.textProperty().getValue()));
        }




    private void setAnomalyHashMap(){
        for (AnomalyReport ar: viewModel.anomalyReportList ){

            if(AnomalyReportHash.containsKey(ar.description) ) {
                    AnomalyReportHash.get(ar.description).add(ar.timeStep);
            }
            else {
                AnomalyReportHash.put(ar.description, new ArrayList<>());
                AnomalyReportHash.get(ar.description).add(ar.timeStep);
            }

        }
    }
    private void AddOnePoint(int time){
        Double tsValue;
        String featureName = featuresList.getSelectionModel().getSelectedItem();
        String correlatedFeatName = " ";

        for(CorrelatedFeatures cf: viewModel.correlatedFeaturesList){

            if (featureName.equals(cf.feature1)){
                correlatedFeatName = cf.feature2;
                break;
            }
            if(featureName.equals(cf.feature2)){
                correlatedFeatName = cf.feature1;
                break;
            }
        }

        cf1.getData().removeAll(series);
        int indexFeature=indexByName(featureName);
        tsValue=Double.parseDouble(viewModel.ts2.lines.get(time).get(indexFeature));
        series.getData().add(new XYChart.Data<>(time,tsValue));
        cf1.getData().add(series);

        if(!correlatedFeatName.equals(" ")){
            cf2.getData().removeAll(series2);
            int correlatedFeature=indexByName(correlatedFeatName);
            tsValue=Double.parseDouble(viewModel.ts2.lines.get(time).get(correlatedFeature));
            series2.getData().add(new XYChart.Data<>(time,tsValue));
            cf2.getData().add(series2);
        }
        currTimeStep++;
    }

    private int indexByName(String name){
        int count=0;
        for(String s:viewModel.ts2.lines.get(0))
        {
            if(s.equals(name))
                return count;
            count++;
        }
        return 0;
    }


    private void AddLine(float x, float y ) {

        anomalySeries.getData().add(new XYChart.Data<>(x,y));
       // anomalyGraph.getData().add(anomalySeries);
    }

    private void paintSimpleAnomalyGraph(){
        anomalySeries.setName("linear Regression Line");
        redPointsSeries.setName("Anomaly Reports");
        anomalyGraph.getData().clear();
        String featureName = featuresList.getSelectionModel().getSelectedItem();
        CorrelatedFeatures correlatedFeat = GetcorFeat(featureName);
        cf1.setTitle(featureName);


        if(correlatedFeat == null){
            cf2.setTitle("No Correlated Feature");
            this.correlatedFeature=null;
            LinePainted=false;
            return;
        }
        if(correlatedFeat.lin_reg==null)
            return;

        cf2.setTitle(correlatedFeat.feature2);
        this.correlatedFeature=correlatedFeat;
        anomalyGraph.getData().removeAll(anomalySeries,redPointsSeries);
        anomalySeries.getData().clear();
        int index1;
        float xS,yS,xE,yE;
        index1=indexByName(correlatedFeat.feature1);


        float min1=Float.parseFloat(viewModel.ts.lines.get(1).get(index1)),
                max1=Float.parseFloat(viewModel.ts.lines.get(1).get(index1));

        for(int i=1;i<viewModel.ts.lines.size();i++){

            if(min1>Float.parseFloat(viewModel.ts.lines.get(i).get(index1)))
                min1=Float.parseFloat(viewModel.ts.lines.get(i).get(index1));
            if(max1<Float.parseFloat(viewModel.ts.lines.get(i).get(index1)))
                max1=Float.parseFloat(viewModel.ts.lines.get(i).get(index1));
        }
        xS=min1;
        yS=correlatedFeat.lin_reg.f(xS);
        xE=max1;
        yE=correlatedFeat.lin_reg.f(xE);

            AddLine(xS, yS);
            AddLine(xE, yE);
           // System.out.println("start point : ("+xS+", "+yS+") end point ( "+xE+", "+yE+")");
            anomalyGraph.getData().add(anomalySeries);

        if(!LinePainted)
            LinePainted=true;
    }

    private void addAnomalyPoints(CorrelatedFeatures cf ) {
        String desc1 = cf.feature1+","+cf.feature2;
        String desc2 = cf.feature2+","+cf.feature1;
        String desc="";
        if(AnomalyReportHash.containsKey(desc1))
            desc = desc1;
        if(AnomalyReportHash.containsKey(desc1+"2"))
            desc=desc1+"2";
        if(AnomalyReportHash.containsKey(desc2))
            desc=desc2;
        if(AnomalyReportHash.containsKey(desc2+"2"))
            desc=desc2+"2";
        if(desc.equals(""))
         return;
        anomalyGraph.getData().removeAll(redPointsSeries);
        redPointsSeries.getData().clear();
       int index1=indexByName(cf.feature1);
       int index2=indexByName(cf.feature2);


       int sizeReport=AnomalyReportHash.get(desc).size();
        for( int i =AnomalyReportHash.get(desc).get(0).intValue();i<timeStep.getValue()&&i<AnomalyReportHash.get(desc).get(sizeReport-1).intValue();i++){
            Double tsValue1 =Double.parseDouble(viewModel.ts2.lines.get(i).get(index1));
            Double tsValue2 =Double.parseDouble(viewModel.ts2.lines.get(i).get(index2));
            redPointsSeries.getData().add(new XYChart.Data<>(tsValue1,tsValue2));
        }
        anomalyGraph.getData().add(redPointsSeries);
    }



    private CorrelatedFeatures GetcorFeat(String s){

        for(CorrelatedFeatures cf : viewModel.correlatedFeaturesList){
            if(cf.feature1.equals(s) || cf.feature2.equals(s))
                return cf;
        }
        return null;
    }

    private void Simple(){
        if(featuresList.getSelectionModel().getSelectedItem()!=null) {
            if (Attribute.equals(featuresList.getSelectionModel().getSelectedItem())) {
                if (currTimeStep + 1 == timeStep.getValue()) {

                    AddOnePoint((int) timeStep.getValue());
                    if(LinePainted)
                        addAnomalyPoints(this.correlatedFeature);

                }
                else if (currTimeStep + 1 < timeStep.getValue()) {
                    pause();
                    for (int i = (int) currTimeStep; i < timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    if(LinePainted)
                        addAnomalyPoints(this.correlatedFeature);

                   play();
                } else if (currTimeStep > timeStep.getValue()) {
                   pause();
                    cf1.getData().clear();
                    series.getData().clear();
                    cf2.getData().clear();
                    series2.getData().clear();
                    for (int i = 1; i <= timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    if(LinePainted)
                        addAnomalyPoints(this.correlatedFeature);
                    currTimeStep = timeStep.getValue();
                   play();
                }
            } else {
               pause();
                paintSimpleAnomalyGraph();
                if(LinePainted)
                    addAnomalyPoints(this.correlatedFeature);
                cf1.getData().clear();
                series.getData().clear();
                cf2.getData().clear();
                series2.getData().clear();
                currTimeStep = 1;
                for (int i = 1; i < timeStep.getValue(); i++) {
                    AddOnePoint(i);
                }
                Attribute = featuresList.getSelectionModel().getSelectedItem();
               play();
            }
        }
    }
    private void Zscore() {
        if(featuresList.getSelectionModel().getSelectedItem()!=null) {
            if (Attribute.equals(featuresList.getSelectionModel().getSelectedItem())) {
                if (currTimeStep + 1 == timeStep.getValue()) {

                    AddOnePoint((int) timeStep.getValue());
                    paintZscoreGraph();
                    paintZscorePoints();

                }
                else if (currTimeStep + 1 < timeStep.getValue()) {
                   pause();
                    for (int i = (int) currTimeStep; i < timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    paintZscoreGraph();
                    paintZscorePoints();
                   play();
                } else if (currTimeStep > timeStep.getValue()) {
                   pause();
                    cf1.getData().clear();
                    series.getData().clear();
                    cf2.getData().clear();
                    series2.getData().clear();
                    for (int i = 1; i <= timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    paintZscoreGraph();
                    paintZscorePoints();

                    currTimeStep = timeStep.getValue();
                  play();
                }
            } else {
              pause();

                paintZscoreGraph();
                paintZscorePoints();
                cf1.getData().clear();
                series.getData().clear();
                cf2.getData().clear();
                series2.getData().clear();
                currTimeStep = 1;
                for (int i = 1; i < timeStep.getValue(); i++) {
                    AddOnePoint(i);
                }
                Attribute = featuresList.getSelectionModel().getSelectedItem();
               play();
            }
        }
    }

    private void paintZscoreGraph(){
        float maxZscore;
        anomalySeries.setName("Z-Score Line");
        redPointsSeries.setName("Anomaly Reports");
        anomalyGraph.getData().clear();
        anomalySeries.getData().clear();
        String featureName = featuresList.getSelectionModel().getSelectedItem();
        int index = indexByName(featureName);
        cf1.setTitle(featureName);
        cf2.setTitle("No Correlated Feature");
        if(plugInChoice!=3)
                maxZscore = viewModel.maxZscore.get(index);
        else {
            CorrelatedFeatures cf = GetcorFeat(featureName);
            for (CorrelatedFeatures correlatedFeatures : viewModel.correlatedFeaturesList) {
                if (correlatedFeatures.feature1.equals(featureName) && correlatedFeatures.feature2.equals(featureName)) {
                    cf = correlatedFeatures;
                    break;
                }
            }
            if (cf == null)
                return;
             maxZscore = cf.corrlation;
        }
        anomalySeries.getData().add(new XYChart.Data<>(1,0));
        anomalySeries.getData().add(new XYChart.Data<>(1,maxZscore));
        anomalyGraph.getData().add(anomalySeries);
    }

    private void paintZscorePoints(){
        String featureName = featuresList.getSelectionModel().getSelectedItem();
        int index = indexByName(featureName);
        if(!AnomalyReportHash.containsKey(featureName)&&!AnomalyReportHash.containsKey(featureName+"1"))
            return;
        anomalyGraph.getData().removeAll(redPointsSeries);
        redPointsSeries.getData().clear();
        if(plugInChoice==3)
            featureName=featureName+"1";
        int sizeReport=AnomalyReportHash.get(featureName).size();
        for(int i=AnomalyReportHash.get(featureName).get(0).intValue();i<timeStep.getValue()
                && i<AnomalyReportHash.get(featureName).get(sizeReport-1).intValue();i++){
            Double tsValue1 =Double.parseDouble(viewModel.ts2.lines.get(i).get(index));
            redPointsSeries.getData().add(new XYChart.Data<>(1,tsValue1));
        }
        anomalyGraph.getData().add(redPointsSeries);
    }
    private void PaintHybridGraph() {

            String Attribute = featuresList.getSelectionModel().getSelectedItem();
            if(Attribute==null)
                return;
            CorrelatedFeatures cf = GetcorFeat(Attribute);
           // anomalyGraph.getData().clear();
            if (cf.feature1.equals(cf.feature2))
                algorithm = 1;
            else {
                if (cf.lin_reg == null)
                    algorithm = 3;
                else algorithm = 2;
            }

        switch (algorithm){
            case 1:
                welzlCircle.setVisible(false);
                anomalyGraph.setVisible(true);
                Zscore();
                break;

            case 2:
                welzlCircle.setVisible(false);
                anomalyGraph.setVisible(true);
                Simple();
                break;

            case 3:
                anomalyGraph.setVisible(false);
                welzlCircle.setVisible(true);
                Welzl();
                break;

        }
    }
//    private void Hybrid(){
//        if(featuresList.getSelectionModel().getSelectedItem()!=null) {
//            if (Attribute.equals(featuresList.getSelectionModel().getSelectedItem())) {
//                if (currTimeStep + 1 == timeStep.getValue()) {
//                    AddOnePoint((int) timeStep.getValue());
//                    PaintHybridGraph();
//                }
//                else if (currTimeStep + 1 < timeStep.getValue()) {
//                   pause();
//                    for (int i = (int) currTimeStep; i < timeStep.getValue(); i++) {
//                        AddOnePoint(i);
//                    }
//                    PaintHybridGraph();
//                   play();
//                } else if (currTimeStep > timeStep.getValue()) {
//                  pause();
//                    cf1.getData().clear();
//                    series.getData().clear();
//                    cf2.getData().clear();
//                    series2.getData().clear();
//                    for (int i = 1; i <= timeStep.getValue(); i++) {
//                        AddOnePoint(i);
//                    }
//                    PaintHybridGraph();
//
//                    currTimeStep = timeStep.getValue();
//                   play();
//                }
//            } else {
//               pause();
//                cf1.getData().clear();
//                series.getData().clear();
//                cf2.getData().clear();
//                series2.getData().clear();
//                currTimeStep = 1;
//                for (int i = 1; i < timeStep.getValue(); i++) {
//                    AddOnePoint(i);
//                }
//                PaintHybridGraph();
//                Attribute = featuresList.getSelectionModel().getSelectedItem();
//               play();
//            }
//        }
//    }

    private void paintWelzlGraph(String name1,String name2) {
        welzlSeries.setName("Welzl Minimum Circle");
        welzlSeries.getData().clear();
        welzlCircle.getData().removeAll(welzlSeries);
        cf1.setTitle(name1);
        cf2.setTitle(name2);
        Circle2D circ=null;
        if(viewModel.circle.containsKey(name1+","+name2))
            circ=viewModel.circle.get(name1+","+name2);
        if(viewModel.circle.containsKey(name2+","+name1))
            circ=viewModel.circle.get(name2+","+name1);
        if(circ==null)
            return;
        welzlSeries.getData().add(new XYChart.Data(circ.center.x,circ.center.y,circ.radius));
        welzlCircle.getData().add(welzlSeries);
    }

    private void paintWelzlPoints(CorrelatedFeatures cf){
        welzlAnomalySeries.setName("Anomaly Points");
        String name1 = cf.feature1+","+cf.feature2+"3";
        String name2 = cf.feature2+","+cf.feature1+"3";
        int index1 = indexByName(cf.feature1);
        int index2 = indexByName(cf.feature2);
        String name=null;
        if(AnomalyReportHash.containsKey(name1))
            name=name1;
        if(AnomalyReportHash.containsKey(name2))
            name=name2;
        if(name==null)
            return;
        welzlAnomalySeries.getData().clear();
        welzlCircle.getData().removeAll(welzlAnomalySeries);

        int sizeReport=AnomalyReportHash.get(name).size();
        for(int i=AnomalyReportHash.get(name).get(0).intValue();i<timeStep.getValue()
               && i<AnomalyReportHash.get(name).get(sizeReport-1).intValue();i++) {
            Double tsValue1 = Double.parseDouble(viewModel.ts2.lines.get(i).get(index1));
            Double tsValue2 = Double.parseDouble(viewModel.ts2.lines.get(i).get(index2));

            welzlAnomalySeries.getData().add(new XYChart.Data(tsValue1, tsValue2));
        }

        welzlCircle.getData().add(welzlAnomalySeries);

    }

    private void Welzl(){
        if(featuresList.getSelectionModel().getSelectedItem()!=null) {
            if (Attribute.equals(featuresList.getSelectionModel().getSelectedItem())) {
                CorrelatedFeatures cf=GetcorFeat(Attribute);
                if (currTimeStep + 1 == timeStep.getValue()) {
                    AddOnePoint((int) timeStep.getValue());
                    paintWelzlGraph(cf.feature1,cf.feature2);
                    paintWelzlPoints(cf);
                }
                else if (currTimeStep + 1 < timeStep.getValue()) {
                    pause();
                    for (int i = (int) currTimeStep; i < timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    paintWelzlGraph(cf.feature1,cf.feature2);
                    paintWelzlPoints(cf);
                    play();
                } else if (currTimeStep > timeStep.getValue()) {
                    pause();
                    cf1.getData().clear();
                    series.getData().clear();
                    cf2.getData().clear();
                    series2.getData().clear();
                    for (int i = 1; i <= timeStep.getValue(); i++) {
                        AddOnePoint(i);
                    }
                    paintWelzlGraph(cf.feature1,cf.feature2);
                    paintWelzlPoints(cf);

                    currTimeStep = timeStep.getValue();
                    play();
                }
            } else {
                pause();
                CorrelatedFeatures cf=GetcorFeat(featuresList.getSelectionModel().getSelectedItem());
                paintWelzlGraph(cf.feature1,cf.feature2);
                paintWelzlPoints(cf);
                cf1.getData().clear();
                series.getData().clear();
                cf2.getData().clear();
                series2.getData().clear();
                currTimeStep = 1;
                for (int i = 1; i < timeStep.getValue(); i++) {
                    AddOnePoint(i);
                }
                Attribute = featuresList.getSelectionModel().getSelectedItem();
                play();
            }
        }
    }

    private void initGauges() {
        gps1.setSkin(new ModernSkin(gps1));
        gps1.setTitle("GPS");
        gps1.setTitleColor(Color.WHITE);
        gps1.setMinValue(0);
        gps1.setMaxValue(100);
        gps1.setAnimated(true);
        gps1.setValueColor(Color.WHITE);
        gps1.setNeedleColor(Color.WHITE);
        gps1.setDecimals(6);
        gps1.setBarColor(Color.RED);


        alt1.setSkin(new ModernSkin(alt1));
        alt1.setTitle("Altitude");
        alt1.setTitleColor(Color.WHITE);
        alt1.setBarColor(Color.RED);
        alt1.setMinValue(-100);
        alt1.setMaxValue(700);
        alt1.setAnimated(true);
        alt1.setValueColor(Color.WHITE);
        alt1.setNeedleColor(Color.WHITE);
        alt1.setDecimals(6);

        speed1.setSkin(new ModernSkin(speed1));
        speed1.setTitle("Speed");
        speed1.setTitleColor(Color.WHITE);
        speed1.setBarColor(Color.RED);
        speed1.setMinValue(0);
        speed1.setMaxValue(100);
        speed1.setAnimated(true);
        speed1.setValueColor(Color.WHITE);
        speed1.setNeedleColor(Color.WHITE);
        speed1.setDecimals(6);

        direction1.setSkin(new ModernSkin(direction1));
        direction1.setTitle("Direction");
        direction1.setTitleColor(Color.WHITE);
        direction1.setBarColor(Color.RED);
        direction1.setMinValue(0);
        direction1.setMaxValue(360);
        direction1.setAnimated(true);
        direction1.setValueColor(Color.WHITE);
        direction1.setNeedleColor(Color.WHITE);
        direction1.setDecimals(6);

        yaw1.setSkin(new ModernSkin(yaw1));
        yaw1.setTitle("Yaw");
        yaw1.setTitleColor(Color.WHITE);
        yaw1.setBarColor(Color.RED);
        yaw1.setMinValue(0);
        yaw1.setMaxValue(360);
        yaw1.setAnimated(true);
        yaw1.setValueColor(Color.WHITE);
        yaw1.setNeedleColor(Color.WHITE);
        yaw1.setDecimals(6);

        roll1.setSkin(new ModernSkin(roll1));
        roll1.setTitle("Roll");
        roll1.setTitleColor(Color.WHITE);
        roll1.setBarColor(Color.RED);
        roll1.setMinValue(0);
        roll1.setMaxValue(360);
        roll1.setAnimated(true);
        roll1.setValueColor(Color.WHITE);
        roll1.setNeedleColor(Color.WHITE);
        roll1.setDecimals(6);

        pitch1.setSkin(new ModernSkin(pitch1));
        pitch1.setTitle("Pitch");
        pitch1.setTitleColor(Color.WHITE);
        pitch1.setBarColor(Color.RED);
        pitch1.setMinValue(0);
        pitch1.setMaxValue(360);
        pitch1.setAnimated(true);
        pitch1.setValueColor(Color.WHITE);
        pitch1.setNeedleColor(Color.WHITE);
        pitch1.setDecimals(6);
    }

//    private void PaintHybridPoints(){
//
//        String Attribute = featuresList.getSelectionModel().getSelectedItem();
//        CorrelatedFeatures cf=GetcorFeat(Attribute);
//        int index1 = indexByName(cf.feature1);
//        int index2 = indexByName(cf.feature2);
//        String name1 = cf.feature1+","+cf.feature2+algorithm;
//        String name2 = cf.feature2+","+cf.feature1+algorithm;
//        String name=null;
//        if(AnomalyReportHash.containsKey(name1))
//            name=name1;
//        if(AnomalyReportHash.containsKey(name2))
//            name=name2;
//        if(name==null)
//            return;
//
//        anomalyGraph.getData().removeAll(redPointsSeries);
//        redPointsSeries.getData().clear();
//        int sizeReport=AnomalyReportHash.get(name).size();
//        for(int i=AnomalyReportHash.get(name).get(0).intValue();i<timeStep.getValue()
//                && i<AnomalyReportHash.get(name).get(sizeReport-1).intValue();i++){
//            Double tsValue1 =Double.parseDouble(viewModel.ts2.lines.get(i).get(index1));
//            Double tsValue2 =Double.parseDouble(viewModel.ts2.lines.get(i).get(index2));
//            redPointsSeries.getData().add(new XYChart.Data<>(tsValue1,tsValue2));
//            GraphicsContext gc=welzlCanvas.getGraphicsContext2D();
//            gc.strokeLine(tsValue1,tsValue2,tsValue1,tsValue2);
//            gc.setStroke(Color.RED);
//
//        }
//        anomalyGraph.getData().add(redPointsSeries);
//    }
}



