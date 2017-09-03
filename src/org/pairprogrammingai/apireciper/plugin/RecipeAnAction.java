package org.pairprogrammingai.apireciper.plugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorGutter;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import org.jetbrains.annotations.NotNull;
import org.pairprogrammingai.apireciper.core.main.AdviserLucene;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class RecipeAnAction extends AnAction {
    /* Defines */
    /*
    private static final String androidSdkPath = "/Users/nishimoto/Documents/Tools/android-sdk-macosx/";
    private static final String androidSdkVersion = "25";
    private static final String luceneIndexPath = "/Users/nishimoto/Desktop/newIndex_type4";
    */

    private Editor editor;
    private Project project;
    private Task.Backgroundable task;
    private boolean isActiveIndicator;

    private final ApiRecipePreferenceConfig config = ApiRecipePreferenceConfig.getInstance();

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);

        if(!config.isUseful()){
            showMessage("Android SDKのパス，もしくはバージョン，Indexのパスが正しく設定されているかをPreferenceで確認してください");
            return ;
        }

        editor = e.getData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        MarkupModel markup = editor.getMarkupModel();
        EditorGutter gutter = editor.getGutter();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        String txt = Messages.showInputDialog(project, "Input your keywords", "ApiReciper", Messages.getQuestionIcon());

        if(txt != null){
            //ファイルの文字列の情報が中に入っている
            String contents = null;
            /*
            try {
                BufferedReader br = new BufferedReader(new FileReader(virtualFile.getPath()));
                String currentLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((currentLine = br.readLine()) != null) {
                    stringBuilder.append(currentLine);
                    stringBuilder.append("\n");
                }
                contents = stringBuilder.toString();
            } catch (IOException e1) {
                return;
            }
            */
            contents = virtualFile.getPath();
            if((contents != null) && (!contents.isEmpty()) && (!txt.isEmpty())){
                markup.removeAllHighlighters();
                send(contents, txt);

                isActiveIndicator = true;
/*
                task = new Task.Backgroundable(project, "Searching..."){

                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        int i = 1;
                        while (isActiveIndicator) {
                            progressIndicator.setFraction(0.10 * i);
                            progressIndicator.setText((10 * i) + "%");
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e1) {
                            }
                            if(i <= 8) {
                                i++;
                            }
                        }

                        progressIndicator.setFraction(1.0);
                        progressIndicator.setText("100%");
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e1) {
                        }
                    }
                };
                ProgressManager.getInstance().run(task);
*/
            }
        }
        System.out.println("End");
    }

    private void send(String sourceName, String keyword){
        AdviserLucene adviserLucene = new AdviserLucene(config.getAndroidSdkPath() + "/", config.getAndroidSdkVersion(), config.getIndexPath());
        adviserLucene.execute(sourceName, keyword, false);
        String result = adviserLucene.getAdvisetoString();
        if(!result.equals("")){
            // success
            System.out.println(result);

            isActiveIndicator = false;
            String[] s = result.split("###");

            if(s.length > 0){
                for(int i = 0; i < s.length; i++){
                    String[] data = s[i].split("@@@");
                    showAdviseMessage(data, i);
                }
                showMessage("Search Finished: Advise " + s.length);
            } else {
                showMessage("Search Finished: No Advise");
            }
        } else {
            // fail
            isActiveIndicator = false;
            showMessage("Sorry! Failed Searching");
        }
    }

    private void showAdviseMessage(String[] advise, int index){
        final Runnable readRunner = new Runnable() {
            @Override
            public void run() {
                MarkupModel markup = editor.getMarkupModel();

                if(advise.length > 1){
                    String[] lineNumbers = advise[0].split(",");

                    // Show Icon
                    for(int i = 0; i < lineNumbers.length; i++){
                        int line = Integer.parseInt(lineNumbers[i]);
                        GutterIconRenderer renderer = new AdviseIconRenderer(advise[1], index, line);
                        RangeHighlighter range = markup.addLineHighlighter(line - 1, HighlighterLayer.CARET_ROW,null);
                        range.setGutterIconRenderer(renderer);
                    }
                }
            }
        };
        WriteCommandAction.runWriteCommandAction(project, readRunner);
    }

    private void showMessage(String text){
        final Runnable readRunner = new Runnable() {
            @Override
            public void run() {
                BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(text, MessageType.INFO, null);
                Balloon balloon = balloonBuilder.createBalloon();
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                balloon.show(new RelativePoint((Component) statusBar, new Point(400, 0)), Balloon.Position.above);
            }
        };
        WriteCommandAction.runWriteCommandAction(project, readRunner);
    }
}
