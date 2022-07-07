/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.ui;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AegisPluginSettings {

  private JTextField emailInput;
  private JLabel     emailLabel;
  private JTextField gitPrivateTokenInput;
  private JLabel     gitPrivateTokenLabel;
  private JTextField javaKotlinTemplateUrlInput;
  private JLabel     javaKotlinTemplateUrlLabel;
  private JPanel     mainPanel;
  private JTextField reactTemplateUrlInput;
  private JLabel     reactTemplateUrlLabel;
  @SuppressWarnings("unused")
  private JPanel     settingsPanel;
  private JTextField userInput;
  private JLabel     userLabel;
  private JTextField vueTemplateUrlInput;
  private JLabel     vueTemplateUrlLabel;
  private JTextField tapdAccountInput;
  private JTextField tapdTokenInput;
  private JLabel     tapdAccountLabel;
  private JLabel tapdTokenLabel;

  public AegisPluginSettings() {
    int width = 200;
    Dimension size = new Dimension(width, -1);
    if (gitPrivateTokenLabel != null) {
      gitPrivateTokenLabel.setPreferredSize(new Dimension(width, -1));
    }
    if (javaKotlinTemplateUrlLabel != null) {
      javaKotlinTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    }
    if (reactTemplateUrlLabel != null) {
      reactTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    }
    if (vueTemplateUrlLabel != null) {
      vueTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    }
    if (userLabel != null) {
      userLabel.setPreferredSize(size);
    }
    if (emailLabel != null) {
      emailLabel.setPreferredSize(size);
    }
    if (tapdAccountLabel != null) {
      tapdAccountLabel.setPreferredSize(size);
    }
    if (tapdTokenLabel != null) {
      tapdTokenLabel.setPreferredSize(size);
    }
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

  public JTextField getTapdAccountInput() {
    return tapdAccountInput;
  }

  public JTextField getTapdTokenInput() {
    return tapdTokenInput;
  }
}
