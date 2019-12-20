/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.ui;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Created by 吴昊 on 18-4-25.
 */
public class AegisPluginSettings {

  private JTextField emailInput;
  private JTextField gitPrivateTokenInput;
  private JTextField javaKotlinTemplateUrlInput;
  private JPanel     mainPanel;
  private JTextField reactTemplateUrlInput;
  private JTextField userInput;
  private JTextField vueTemplateUrlInput;
  private JLabel     emailLabel;
  private JLabel     gitPrivateTokenLabel;
  private JLabel     javaKotlinTemplateUrlLabel;
  private JLabel     reactTemplateUrlLabel;
  @SuppressWarnings("unused")
  private JPanel     settingsPanel;
  private JLabel     userLabel;
  private JLabel     vueTemplateUrlLabel;

  public AegisPluginSettings() {
    int width = 200;
    Dimension size = new Dimension(width, -1);
    gitPrivateTokenLabel.setPreferredSize(new Dimension(width, -1));
    javaKotlinTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    reactTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    vueTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    userLabel.setPreferredSize(size);
    emailLabel.setPreferredSize(size);
  }

  public JTextField getEmailInput() {
    return emailInput;
  }

  public JTextField getGitPrivateTokenInput() {
    return gitPrivateTokenInput;
  }

  public JTextField getJavaKotlinTemplateUrlInput() {
    return javaKotlinTemplateUrlInput;
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public JTextField getReactTemplateUrlInput() {
    return reactTemplateUrlInput;
  }

  public JTextField getUserInput() {
    return userInput;
  }

  public JTextField getVueTemplateUrlInput() {
    return vueTemplateUrlInput;
  }

}

