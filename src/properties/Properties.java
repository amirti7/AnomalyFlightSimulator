package properties;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Properties implements Serializable {

    protected int port;
    protected String host;
    protected HashMap<String,Attribute> attributes;
    protected int rate;




    public Properties() {attributes=new HashMap<>();}

    public int getRate() { return rate; }
    public void setRate(int rate) { this.rate = rate; }
    public int getPort() {return port;}
    public void setPort(int port) {this.port = port;}
    public String getHost() {return host;}
    public void setHost(String host) {this.host = host;}
    public HashMap<String, Attribute> getAttributes() { return attributes; }
    public void setAttributes(HashMap<String,Attribute> attributes) { this.attributes = attributes; }


    public void serializeToXML (String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        XMLEncoder encoder = new XMLEncoder(fos);
        encoder.writeObject(this);
        encoder.close();
        fos.close();
    }

    public Boolean deserializeFromXML(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XMLDecoder decoder = new XMLDecoder(fis);
            Properties decodedSettings =(Properties)decoder.readObject();
            decoder.close();
            fis.close();
            this.setFromAnotherProperties(decodedSettings);
            serializeToXML("settings.xml");//Save all information in a separate XML file for future execution.
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void setFromAnotherProperties(Properties decodedSettings) {
        setHost(decodedSettings.getHost());
        setPort(decodedSettings.getPort());
        setRate(decodedSettings.getRate());
        setAttributes(decodedSettings.getAttributes());
    }


}

