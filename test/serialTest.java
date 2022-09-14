// package serialtalk;
//CONVERT THIS TO USE THE ONE YOU ALREADY HAVE WITH MAVEN?
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
//import processing.app.Preferences;

public class Main
{
    static InputStream input;
    static OutputStream output;

    public static void main(String[] args) throws Exception
    {
        //Preferences.init();
        //System.out.println("Using port: " + Preferences.get("serial.port"));
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier("/dev/ttyACM1");

        SerialPort port = (SerialPort)portId.open("serial talk", 4000);
        input = port.getInputStream();
        output = port.getOutputStream();
        //port.sets
        port.setSerialPortParams(115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        
        //System.out.write(buf, off, len);

        String sCommand = "O,0,2,0";
        byte[] b = sCommand.getBytes();
       
        output.write(b);
        Thread.sleep(500);

        sCommand = "O,0,0,0";
        sCommand = "1";
        b = sCommand.getBytes();
        output.write(b);
        Thread.sleep(500);

        int i = 0;

        while(i++ < 10000)
        {
            sCommand = "V";
            b = sCommand.getBytes();
            output.write(b);
            Thread.sleep(500);
            
            while(input.available()>0)
            {
                System.out.print((char)(input.read()));
            }
        }
        
        port.close();
    }


}