package com.galaxybruce.android.codegenerator.plugin.actions;

import com.galaxybruce.android.codegenerator.plugin.util.FileUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * dialog模板
 *
 * 参考文章：
 * [Intellij IDEA 插件开发秘籍](https://www.cnblogs.com/kancy/p/10654569.html)
 */
public abstract class AndroidUiTemplateAction extends AnAction {

    private static final Logger LOG = Logger.getInstance("AndroidUiTemplateAction");


    protected Project project;
    protected String psiPath;

    protected JDialog jFrame;
    protected JTextField nameTextField;
    protected ButtonGroup templateGroup;

    protected JCheckBox layoutBox;
    protected JCheckBox kotlinBox;
    protected JCheckBox mvvmBox;

    protected String javaParentPath;
    protected String layoutFileName;
    protected String modulePackage;


    protected KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                save();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    };

    protected ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Cancel")) {
                dispose();
            } else {
                save();
            }
        }
    };

    private void dispose() {
        jFrame.dispose();
    }
    private void save() {
        if (nameTextField.getText() == null || "".equals(nameTextField.getText().trim())) {
            Messages.showInfoMessage(project, "Please enter the name", "Info");
            return;
        }
        dispose();
        clickCreateFile();
        project.getProjectFile().refresh(false, true);
        Messages.showInfoMessage(project, "Enjoy Coding~", "Success!");
    }

    protected abstract void clickCreateFile();

    protected void generateFile(String srcFile, String psiPath, String featureDir, String fileName) {
        String currentDirPath = psiPath;
        if(featureDir != null) {
            currentDirPath = currentDirPath  + File.separator + featureDir;
        }

        String content = "";

        if(kotlinBox != null && kotlinBox.isSelected()) {
            String tempSrcFile = srcFile.replaceAll("\\.java\\.txt", ".kt.txt");
            String tempFileName = fileName.replaceAll("\\.java", ".kt");
            content = FileUtils.readFile(this.getClass(), tempSrcFile);

            if(content != null && !"".equals(content.trim())) {
                srcFile = tempSrcFile;
                fileName = tempFileName;
            }
        }

        if(content == null || "".equals(content.trim())) {
            content = FileUtils.readFile(this.getClass(), srcFile);
        }

        fileName = nameTextField.getText().trim() + fileName;
        content = dealFile(psiPath, currentDirPath, content, false);
        FileUtils.writeToFile(content, currentDirPath, fileName);
    }

    /**
     * 生成布局文件
     * @param srcFile
     * @param psiPath
     */
    protected void generateLayoutFile(String srcFile, String psiPath) {
        String currentDirPath = javaParentPath + "res" + File.separator + "layout";
        String fileName = layoutFileName + ".xml";
        String content = FileUtils.readFile(this.getClass(), srcFile);
        content = dealFile(psiPath, currentDirPath, content, true);
        FileUtils.writeToFile(content, currentDirPath, fileName);
    }

    /**
     * 生成布局文件名称，不带.xml后缀，并且是经过驼峰转下划线处理
     * @return
     */
    protected String makeLayoutFileName() {
        if (layoutBox.isSelected()) {
            String fileName = nameTextField.getText().trim() + "Layout";
            return FileUtils.camelToUnderline(fileName);
        }
        return "";
    }

    protected String dealFile(String psiPath, String currentDirPath, String content, boolean isLayout) {
        if(!isLayout) {
            content = FileUtils.makePackageString(currentDirPath) + content;
        }
        content = content.replaceAll("\\$\\{name\\}", nameTextField.getText());
        content = content.replaceAll("\\$\\{package\\}", FileUtils.pathToPackage(psiPath));

        if(modulePackage != null && !"".equals(modulePackage)) {
            content = content.replaceAll("\\$\\{importR\\}", "import " + modulePackage + ".R;");
            content = content.replaceAll("\\$\\{importBR\\}", "import " + modulePackage + ".BR;");
        } else {
            content = content.replaceAll("\\$\\{importR\\}", "");
            content = content.replaceAll("\\$\\{importBR\\}", "");
        }

        // 布局文件名称需要驼峰转下划线
        if (layoutBox.isSelected()) {
            content = content.replaceAll("\\$\\{layoutName\\}", layoutFileName);
        }
        return content;
    }

}
