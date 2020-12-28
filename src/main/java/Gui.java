import lab1.manager.ConjunctionManager;
import lab1.manager.DisjunctionManager;
import lab1.manager.DoubleManager;
import lab1.manager.IntManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PipedInputStream;

class Gui {
    static Thread thread;
    static Frame f = new Frame("Lab1");
    static PipedInputStream input;
    static TextField resultText = new TextField();
    static Button startButton = new Button("Start");

    private static void getRes(){
        while(true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {}
            try {
                if (input!=null && input.available() != 0) {
                    try (ObjectInput in = new ObjectInputStream(input)) {
                        resultText.setText(in.readObject().toString());
                    } finally {
                        startButton.setEnabled(true);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        String s[] = {"AND", "OR", "INT", "DOUBLE"};
        Label argL= new Label();
        argL.setText("X:");
        argL.setBounds(20,50, 30,20);
        TextField argText = new TextField();
        argText.setBounds(50,50, 150,20);
        startButton.setBounds(20,150,100,30);
        JComboBox operationBox = new JComboBox(s);
        operationBox.setBounds(50,100,150,20);
        resultText.setEditable(false);
        resultText.setBounds(150,150,100,30);

        startButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                int arg = Integer.parseInt(argText.getText());
                input = new PipedInputStream();
                switch (operationBox.getSelectedIndex()) {
                    case 0 -> thread = new Thread(new ConjunctionManager(arg, input));
                    case 1 -> thread = new Thread(new DisjunctionManager(arg, input));
                    case 2 -> thread = new Thread(new IntManager(arg, input));
                    case 3 -> thread = new Thread(new DoubleManager(arg, input));
                }
                startButton.setEnabled(false);
                resultText.setText("");
                thread.start();
            }
        });

        argText.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if(thread != null) thread.interrupt();
                    startButton.setEnabled(true);
                }
            }
            public void keyReleased(KeyEvent e) {}
        });

        f.add(startButton);f.add(argText);
        f.add(argL); f.add(operationBox);
        f.add(resultText);
        f.setSize(300,200);
        f.setLayout(null);
        f.setVisible(true);

        getRes();
    }
}
