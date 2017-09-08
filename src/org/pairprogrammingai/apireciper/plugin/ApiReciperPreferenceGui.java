package org.pairprogrammingai.apireciper.plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ApiReciperPreferenceGui extends Frame implements ActionListener{
    private JPanel panel1;
    private JTextField textField1;
    private JTextField textField2;
    private JButton selectButton;
    private JButton selectButton1;
    private JTextField textField3;
    private JCheckBox checkBox1;

    public JPanel getRootPanel() {
        return panel1;
    }

    public ApiReciperPreferenceGui(ApiRecipePreferenceConfig config){
        reset(config);

        selectButton.addActionListener(this);
        selectButton1.addActionListener(this);
        checkBox1.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(selectButton)){
            selectFolderAction(textField1);
        } else if(e.getSource().equals(selectButton1)){
            selectFolderAction(textField2);
        } else if(e.getSource().equals(checkBox1)){
            if(checkBox1.isSelected()){
                checkBox1.setText("Enable");
            } else {
                checkBox1.setText("Disable");
            }
        }
    }

    private void selectFolderAction(JTextField textfield){
        JFileChooser fileChooser = new JFileChooser(textfield.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int selected = fileChooser.showSaveDialog(this);
        if(selected == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            textfield.setText(file.getAbsolutePath());
        }
    }

    public void apply(ApiRecipePreferenceConfig config){
        config.setAndroidSdkPath(textField1.getText());
        config.setIndexPath(textField2.getText());
        config.setAndroidSdkVersion(textField3.getText());
        config.setIsSupportPPAI(checkBox1.isSelected());
    }

    public void reset(ApiRecipePreferenceConfig config){
        textField1.setText(config.getAndroidSdkPath());
        textField2.setText(config.getIndexPath());
        textField3.setText(config.getAndroidSdkVersion());
        checkBox1.setSelected(config.getIsSupportPPAI());
        if(checkBox1.isSelected()){
            checkBox1.setText("Enable");
        } else {
            checkBox1.setText("Disable");
        }
    }

    public boolean isModified(ApiRecipePreferenceConfig config){
        return !(config.getAndroidSdkPath().equals(textField1.getText())
                && config.getIndexPath().equals(textField2.getText())
                && config.getAndroidSdkVersion().equals(textField3.getText())
                && config.getIsSupportPPAI() == checkBox1.isSelected());
    }
}
