/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package error.java;

/**
 * @author 吴昊
 * @since 1.3.1
 */
class ConstatnArgumentExample {

  public void test(String text) {
    showMeTheCode("");
    int count = 10;
    for (int i = 1; i < count; i++) {
      return;
    }
    String content = "";
    content = content.replaceAll("“|”", "").replaceAll("[（\\(].*?[）\\)]", "").replaceAll("普通", "").replaceAll("出租", "").replaceAll("非营运", "").replaceAll("驾驶证", "")
                  .replaceAll("厢式", "").replaceAll("箱式", "").replaceAll("临时", "").replaceAll("治安巡逻套", "").replaceAll(" ", "").replaceAll("两厢", "").replaceAll("·", "").replaceAll("\\.", "")
                  .replaceAll("出租", "").replaceAll("自备", "").replaceAll("营运", "").replaceAll("未悬挂", "").replaceAll("未挂", "");

  }

  public void showMeTheCode(String text) {
  }

}

