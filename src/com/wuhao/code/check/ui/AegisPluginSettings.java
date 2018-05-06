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

  public JPanel     mainPanel;
  private JPanel     settingsPanel;
  public JTextField gitPrivateTokenInput;
  public JTextField vueTemplateUrlInput;
  public JTextField reactTemplateUrlInput;
  public JTextField javaKotlinTemplateUrlInput;
  private JLabel     gitPrivateTokenLabel;
  private JLabel     vueTemplateUrlLabel;
  private JLabel     reactTemplateUrlLabel;
  private JLabel     javaKotlinTemplateUrlLabel;

  public AegisPluginSettings() {
    int width = 200;
    gitPrivateTokenLabel.setPreferredSize(new Dimension(width, -1));
    javaKotlinTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    reactTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
    vueTemplateUrlLabel.setPreferredSize(new Dimension(width, -1));
  }

}
