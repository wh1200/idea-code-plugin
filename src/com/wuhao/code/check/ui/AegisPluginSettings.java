/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.intellij.database.model.DasDataSource;
import com.intellij.database.psi.DataSourceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class AegisPluginSettings {

  private JTextField     emailInput;
  private JLabel         emailLabel;
  private JTextField     gitPrivateTokenInput;
  private JLabel         gitPrivateTokenLabel;
  private JTextField     javaKotlinTemplateUrlInput;
  private JLabel         javaKotlinTemplateUrlLabel;
  private JPanel         mainPanel;
  private JTextField     reactTemplateUrlInput;
  private JLabel         reactTemplateUrlLabel;
  @SuppressWarnings("unused")
  private JPanel         settingsPanel;
  private JTextField     userInput;
  private JLabel         userLabel;
  private JTextField     vueTemplateUrlInput;
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
