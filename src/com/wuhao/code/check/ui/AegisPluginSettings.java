/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.ui;

import javax.swing.*;
import java.awt.*;


/**
 * Created by 吴昊 on 18-4-25.
 */
public class AegisPluginSettings {

  public  JTextField emailInput;
  public  JTextField gitPrivateTokenInput;
  public  JTextField javaKotlinTemplateUrlInput;
  public  JPanel     mainPanel;
  public  JTextField reactTemplateUrlInput;
  public  JTextField userInput;
  public  JTextField vueTemplateUrlInput;
  private JLabel     emailLabel;
  private JLabel     gitPrivateTokenLabel;
  private JLabel     javaKotlinTemplateUrlLabel;
  private JLabel     reactTemplateUrlLabel;
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

}

