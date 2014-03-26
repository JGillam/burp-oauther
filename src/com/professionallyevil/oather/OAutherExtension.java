package com.professionallyevil.oather;

import burp.*;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpRequest;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OAutherExtension implements IBurpExtender, IHttpListener {
    private JPanel mainPanel;
    private JCheckBox enableOAuthWrappingCheckBox;
    private IBurpExtenderCallbacks callbacks;
    private JTextField textConsumerKey;
    private JTextField textConsumerSecret;
    private JTextField textTokenWithSecret;
    private JTextField textTokenSecret;
    private JButton lockButton;
    private JLabel statusLabel;
    private boolean isLocked = false;
    private OAuthConsumer consumer = null;


    public OAutherExtension() {
    }

    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        callbacks.setExtensionName("OAuther");

        callbacks.addSuiteTab(new ITab(){

            @Override
            public String getTabCaption() {
                return "OAuther";
            }

            @Override
            public Component getUiComponent() {
                return mainPanel;
            }
        });

        enableOAuthWrappingCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (enableOAuthWrappingCheckBox.isSelected()) {
                    callbacks.registerHttpListener(OAutherExtension.this);
                    callbacks.issueAlert("OAuther enabled");
                }else{
                    callbacks.removeHttpListener(OAutherExtension.this);
                    callbacks.issueAlert("OAuther disabled");
                }
            }
        });
        lockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isLocked){
                    textConsumerKey.setEnabled(true);
                    textConsumerSecret.setEnabled(true);
                    textTokenWithSecret.setEnabled(true);
                    textTokenSecret.setEnabled(true);
                    lockButton.setText("Lock");
                    statusLabel.setText("Unlocked - enter values and press \"Lock\"");
                    isLocked = false;
                }else{
                    if(textConsumerKey.getText().isEmpty()){
                        statusLabel.setText("<html><span color='red'>Please enter a value for 'Consumer Key'</span></html>");
                    }else if(textConsumerSecret.getText().isEmpty()){
                        statusLabel.setText("<html><span color='red'>Please enter a value for 'Consumer Secret'</span></html>");
                    }else if(textTokenWithSecret.getText().isEmpty()){
                        statusLabel.setText("<html><span color='red'>Please enter a value for 'Token'</span></html>");
                    }else if(textTokenSecret.getText().isEmpty()){
                        statusLabel.setText("<html><span color='red'>Please enter a value for 'Token Secret'</span></html>");
                    }else{
                        try {
                            consumer = new DefaultOAuthConsumer(textConsumerKey.getText(), textConsumerSecret.getText());
                            consumer.setTokenWithSecret(textTokenWithSecret.getText(), textTokenSecret.getText());
                            lockButton.setText("Unlock");
                            statusLabel.setText("<html><span color='blue'>Value locked in!  Press unlock to make changes.</span></html>");
                            textConsumerKey.setEnabled(false);
                            textConsumerSecret.setEnabled(false);
                            textTokenWithSecret.setEnabled(false);
                            textTokenSecret.setEnabled(false);
                            isLocked = true;
                        } catch (Throwable e1) {
                            statusLabel.setText("<html><span color='red'>There is a problem: "+e1.getMessage()+"</span></html>");
                        }

                    }
                }

            }
        });
        callbacks.printOutput("OAuther extension loaded: v1.0");

    }


    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (messageIsRequest && isLocked) {
            HttpRequest req = new SignpostBurpHttpRequestWrapper(messageInfo);

            try {
                consumer.sign(req);
            } catch (OAuthException e) {
                callbacks.printError("OAuth"+e.getMessage());
                callbacks.issueAlert("OAuther could not sign request: "+e.getMessage());
            }

        }
    }
}