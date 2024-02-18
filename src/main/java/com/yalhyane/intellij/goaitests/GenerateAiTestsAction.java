package com.yalhyane.intellij.goaitests;

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.yalhyane.intellij.goaitests.settings.AppSettingsState;
import com.yalhyane.intellij.goaitests.settings.OpenSettingsAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class GenerateAiTestsAction extends AnAction  {


    // constants
    public static final String ACTION_ID = "Go.GenerateAiTests";
    public static final String PLUGIN_ID = "com.yalhyane.intellij.goAiDocComment.go-ai-doc-comment";
    private static final String MISSING_SETTINGS_NOTIFICATION_TITLE = "Missing settings";
    private static final String UPDATE_SETTINGS_NOTIFICATION_CONTENT = "Please configure openAI token and model under AI comment settings";
    private static final String INVALID_ELEMENT_NOTIFICATION_CONTENT = "Could not detect code block";
    private static final String INVALID_BLOCK_NOTIFICATION_CONTENT = "Please select code block or place the caret inside a function";
    private static final String GENERAL_ERROR_NOTIFICATION_TITLE = "Ai tests";
    private static final String INVALID_EDITOR_OR_PSI_FILE_NOTIFICATION_CONTENT = "Could not detect Editor/File";
    private static final String COULD_NOT_CREATE_TEST_FILE_NOTIFICATION_CONTENT = "Could not create test file";
    static final AnAction OPEN_SETTINGS_ACTION = new OpenSettingsAction();

    private AppSettingsState settings;
    private PromptService openAIService;


    public static GenerateAiTestsAction getInstance() {
        return (GenerateAiTestsAction) ActionManager.getInstance().getAction(ACTION_ID);
    }

    public GenerateAiTestsAction() {
        super();
        this.reloadSettings();

    }


    public void reloadSettings() {
        this.settings = AppSettingsState.getInstance();
        this.openAIService = new PromptService(settings.openAiToken, settings.openAiModel);
        System.out.println("Loaded action: " + settings.openAiModel);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {


        if (Objects.equals(this.settings.openAiToken, "") || Objects.equals(this.settings.openAiModel, "")) {
            this.showErrorNotification(MISSING_SETTINGS_NOTIFICATION_TITLE, UPDATE_SETTINGS_NOTIFICATION_CONTENT, OPEN_SETTINGS_ACTION);
            return;
        }


        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_EDITOR_OR_PSI_FILE_NOTIFICATION_CONTENT);
            return;
        }


        CaretModel caret = editor.getCaretModel();
        String blockCode;
        String blockName;
        String blockType;
        // in case of selection
        if (editor.getSelectionModel().hasSelection()) {
            blockName = psiFile.getName();
            blockCode = editor.getSelectionModel().getSelectedText();
            blockType = "Golang";
        } else {

            // handle selection
            // handle function or method
            PsiElement element1 = psiFile.findElementAt(caret.getOffset());
            if (element1 == null) {
                this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_ELEMENT_NOTIFICATION_CONTENT);
                return;
            }

            GoFunctionOrMethodDeclaration pe = PsiTreeUtil.getParentOfType(element1, GoFunctionOrMethodDeclaration.class);
            if (pe == null) {
                this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_BLOCK_NOTIFICATION_CONTENT);
                return;
            }
            blockName = pe.getName();
            blockCode = pe.getText();
            blockType = "function";
        }



        if (blockCode == null || blockCode.trim().isEmpty()) {
            this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_BLOCK_NOTIFICATION_CONTENT);
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {


            try {
                // prepare tests file
                // Get the current file name.
                String fileName = psiFile.getName().replace(".go", "");

                // Create a new file name with the name `{current_file_name}_test.go`.
                String newFileName = fileName + "_test.go";


                PsiDirectory psiDir = psiFile.getContainingDirectory();

                PsiFile testPsiFile = psiDir.findFile(newFileName);
                if (testPsiFile == null) {
                    try {
                        psiDir.checkCreateFile(newFileName);
                        testPsiFile = psiDir.createFile(newFileName);
                    } catch (IncorrectOperationException e) {
                        this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, e.getMessage());
                    }
                }
                if (testPsiFile == null) {
                    this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, COULD_NOT_CREATE_TEST_FILE_NOTIFICATION_CONTENT);
                }


                Document testsPsiDoc = PsiDocumentManager.getInstance(project).getDocument(Objects.requireNonNull(testPsiFile));
                if (testsPsiDoc == null) {
                    this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, COULD_NOT_CREATE_TEST_FILE_NOTIFICATION_CONTENT);
                    return;
                }


                String tests = this.getTests(blockName, blockCode, blockType);


                int cursorIndex = testsPsiDoc.getTextLength();
                testsPsiDoc.insertString(cursorIndex, tests);

                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, testPsiFile.getVirtualFile(), cursorIndex);
                fileEditorManager.openTextEditor(descriptor, true);

            } catch (Exception e) {
                this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, e.getMessage());
            }

        });

    }

    private String getTests(String funcName, String funcBody, String blockType) {

        try {
            String testsCode = openAIService.execute(funcBody, blockType);
            testsCode = testsCode.trim();
            if (testsCode.startsWith("\"")) {
                testsCode = testsCode.substring(1);
            }
            if (testsCode.endsWith("\"")) {
                testsCode = testsCode.substring(0, testsCode.length() - 1);
            }
            return testsCode;
        } catch (Exception e) {
            this.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, e.getMessage());
            return "";
        }
    }


    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        // Set the availability based on whether a project is open
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabled(editor != null && psiFile != null && this.settings.openAiToken != null);
    }


    private void showErrorNotification(String title, String content, @Nullable AnAction... actions) {
        Notification notification = new Notification(PLUGIN_ID, title, content, NotificationType.ERROR);
        if (actions != null) {
            notification.addActions((Collection<? extends AnAction>) Arrays.asList(actions));
        }
        Notifications.Bus.notify(notification);
    }
}
