package View;

import Model.Model;
import ViewModel.ViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import properties.Attribute;
import properties.Properties;

import java.util.HashMap;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxml = new FXMLLoader();
        Parent root = fxml.load(getClass().getResource("sample.fxml").openStream());
//        XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File("./resources/properties.xml")));
//        Properties myProperties =(Properties)decoder.readObject();
//        decoder.close();
        ViewModel viewModel=new ViewModel();
        Model model=new Model(viewModel.timeStep);
        Controller controller=fxml.getController();
        controller.init(viewModel);

        HashMap<String,Attribute> attributes = new HashMap<>();

//        attributes.put("rudder", new Attribute("rudder",-1,1 ));
//        attributes.put ("yaw",new Attribute("yaw",0,360));
//        attributes.put("roll",new Attribute("roll",0,360));
//        attributes.put("pitch",new Attribute("pitch",0,360));
//        attributes.put("throttle",new Attribute("throttle",0,1));
//        attributes.put("aileron",new Attribute("aileron",-1,1));
//
//       Properties myProperties = new Properties();
//       myProperties.setPort(5400);
//       myProperties.setHost("127.0.0.1");
//       myProperties.setRate(100);
//       myProperties.setAttributes(attributes);
//
//       myProperties.serializeToXML("./resources/properties.xml");

        primaryStage.setTitle("Flight Simulator");
        primaryStage.setScene(new Scene(root, 1000, 650));
        primaryStage.show();
    }


    public static void main(String[] args) { launch(args); }
}
