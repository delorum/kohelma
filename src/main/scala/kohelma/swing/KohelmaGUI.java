package kohelma.swing;

import kohelma.Elman;
import kohelma.KohelmaImages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class KohelmaGUI {
    JPanel mainPanel;
    JTabbedPane tabbedPane1;
    JTextField a5625TextField;
    JTextField a3TextField;
    JTextField a10TextField;
    JComboBox comboBox1;
    JButton удалитьButton;
    JButton добавитьButton;
    JTextField a100000000TextField;
    JTextField a075TextField;
    JTextField a002TextField;
    JButton начатьButton;
    JButton сохранитьСетьButton;
    JButton загрузитьButton;
    JButton загрузитьButton1;
    JButton сохранитьButton;
    JTextPane ееСестраЗваласьТатьянаTextPane;
    JPanel helpPanel;
    JTextPane helpTextPane;
    JPanel trainingPanel;
    JPanel settingsPanel;
    JPanel recognitionPanel;
    JLabel inputNeuronsSettingLabel;
    JLabel hiddenNeuronsSettingLabel;
    JLabel outputNeuronsSettingLabel;

    /*public KohelmaGUI() {

        *//*загрузитьButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser loadElm = new JFileChooser();
                if (loadElm.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
                    File elman_file = loadElm.getSelectedFile();
                    elman = Elman.apply(elman_file.getAbsolutePath());
                    inputNeuronsSettingLabel.setText(elman.);
                }
            }
        });*//*
    }*/

    /*private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Handwriting OCR");
        KohelmaGUI gui = new KohelmaGUI();
        frame.setContentPane(gui.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        frame.setLocation((int)centerPoint.getX() - 400, (int)centerPoint.getY() - 300);
        frame.setVisible(true);
    }*/
}
