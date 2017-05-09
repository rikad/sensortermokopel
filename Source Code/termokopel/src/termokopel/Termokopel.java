/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package termokopel;

import gnu.io.*;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
/**
 *
 * @author elawliet
 */
public class Termokopel implements SerialPortEventListener {
    
    //untuk window
    Termo_frame window = null;    
    //for containing the ports that will be found
    private Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    private final HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";
    String data1 = "";

    
    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;
    
    //record true/false
    public boolean record1 = false;
    public boolean record2 = false;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    final static int NEW_LINE_ASCII = 10;

    //inisialisasi window kontruktor
    public Termokopel(Termo_frame window) {
        this.window = window;
    }

    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                window.jListports.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect()
    {
        String selectedPort = (String)window.jListports.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("termokopel", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;

            //for controlling GUI elements
            setConnected(true);

            //logging
            logText = selectedPort + " opened successfully.";
            window.jLog.setForeground(Color.black);
            window.jLog.append(logText + "\n");

        }
        catch (PortInUseException e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            
            window.jLog.setForeground(Color.RED);
            window.jLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            window.jLog.append(logText + "\n");
            window.jLog.setForeground(Color.RED);
        }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }
    
    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            //writeData(0, 0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);

            logText = "Disconnected.";
            window.jLog.setForeground(Color.red);
            window.jLog.append(logText + "\n");
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            window.jLog.setForeground(Color.red);
            window.jLog.append(logText + "\n");
        }
    }    

    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            //writeData(0, 0);
            
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            window.jLog.setForeground(Color.red);
            window.jLog.append(logText + "\n");
            return successful;
        }
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            window.jLog.setForeground(Color.red);
            window.jLog.append(logText + "\n");
        }
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                byte singleData = (byte)input.read();
                
                switch (singleData) {
                //47 ascii == / character, split into two data
                    case NEW_LINE_ASCII:
                        System.out.println("data1 = "+data1);
                        window.jData2.setText(data1);
                        data1 = "";

                        if (record2) {
                            window.RefreshData2();
                        }
                        //logText = "New Data Received \n";
                        //window.jLog.append(logText);
                        break;
                    case 47:
                        System.out.println("data2 = "+data1);
                        window.jData1.setText(data1);
                        data1 = "";

                        if (record1) {
                            window.RefreshData1();
                        }
                        break;
                    default:
                        data1 += new String(new byte[] {singleData});
                        break;
                }
                
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                window.jLog.setForeground(Color.red);
                window.jLog.append(logText + "\n");
            }
        }
    }
    
    public static void main(String[] args) {
        new Termo_frame().setVisible(true);
        System.out.println("Started...");
    }
}
