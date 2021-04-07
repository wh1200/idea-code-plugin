package com.wuhao.code.check;

import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern.Capture;
import com.intellij.patterns.PsiFilePattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.lang.expr.VueJSLanguage;
import org.jetbrains.vuejs.lang.html.VueLanguage;

public class PsiPatterns2 {

  public static Capture<JSNewExpression> newVuePattern() {
    return PlatformPatterns.psiElement(JSNewExpression.class).withChild(
        PlatformPatterns.psiElement(JSReferenceExpression.class).withText("Vue")
    );
  }

  public static PsiFilePattern.Capture<PsiFile> vueFile() {
    return PlatformPatterns.psiFile().withLanguage(VueLanguage.Companion.getINSTANCE())
        .andOr(PlatformPatterns.psiFile().withLanguage(VueJSLanguage.Companion.getINSTANCE()));
  }

  public static @NotNull Capture<PsiElement> vueLangPattern() {
    return PlatformPatterns.psiElement()
        .inFile(vueFile());
  }

  public static @NotNull Capture<XmlTag> vueScriptTag() {
    return PlatformPatterns.psiElement(XmlTag.class).withParent(vueLangPattern())
        .withName(HtmlUtil.SCRIPT_TAG_NAME);
  }

  public static Capture<LeafPsiElement> leaf() {
    return PlatformPatterns.psiElement(LeafPsiElement.class);
  }

}
