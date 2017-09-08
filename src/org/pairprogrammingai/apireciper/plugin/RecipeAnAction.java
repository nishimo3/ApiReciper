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

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.pairprogrammingai.apireciper.core.main.AdviserLucene;

import java.awt.*;
import java.net.URL;

public class RecipeAnAction extends AnAction {
    private Editor editor;
    private Project project;
    private Task.Backgroundable task;

    private final ApiRecipePreferenceConfig config = ApiRecipePreferenceConfig.getInstance();

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        editor = e.getData(CommonDataKeys.EDITOR);
        if(!config.isUseful()){
            showMessage("Android SDKのパス，もしくはバージョン，Indexのパスが正しく設定されているかをPreferenceで確認してください");
            return ;
        }

        Document document = editor.getDocument();
        MarkupModel markup = editor.getMarkupModel();
        EditorGutter gutter = editor.getGutter();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        String txt = Messages.showInputDialog(project, "Input your keywords", "ApiReciper", Messages.getQuestionIcon());


        String contents = virtualFile.getPath();
        if((contents != null) && (!contents.isEmpty()) && (!txt.isEmpty())){
            markup.removeAllHighlighters();
            send(contents, txt);
        }
    }

    private void send(String sourceName, String keyword){
        AdviserLucene adviserLucene = new AdviserLucene(config.getAndroidSdkPath() + "/", config.getAndroidSdkVersion(), config.getIndexPath());
        adviserLucene.execute(sourceName, keyword, false);
        String result = adviserLucene.getAdvisetoString();

        String message = "";
        if(!result.equals("")){
            // success
            System.out.println(result);

            String[] s = result.split("###");
            if(s.length > 0){
                for(int i = 0; i < s.length; i++){
                    String[] data = s[i].split("@@@");
                    showAdviseMessage(data, i);
                }
                message = "ApiReciper：「" + keyword + "」の検索完了です．アドバイスは" + s.length + "件です";
            } else {
                message = "ApiReciper：「" + keyword + "」の検索完了です．アドバイスはありません";
            }
        } else {
            // fail
            message = "ApiReciper：「" + keyword + "」の検索完了です．アドバイスはありません";
        }

        if(ApiRecipePreferenceConfig.getInstance().getIsSupportPPAI()) {
            notifyPairProgrammingAI(message);
        } else {
            showMessage(message);
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

    private void notifyPairProgrammingAI(String data){
        try{
            String pName = "ReceiveMessageServer";
            String pIpAddr = "127.0.0.1";
            String pPort = "5678";

            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            System.out.println("http://"+ pIpAddr +":"+ pPort +"/xmlrpc");

            config.setServerURL(new URL("http://"+ pIpAddr +":"+ pPort +"/xmlrpc"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            Object[] params = new Object[]{new String(data)};
            client.executeAsync(pName + "." + "setMessage", params, new AsyncCallback() {

                @Override
                public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                    System.out.println((String) pResult);
                }

                @Override
                public void handleError(XmlRpcRequest pRequest, Throwable pError) {
                    System.out.println("handleError " + pError.getMessage());
                    showMessage(data);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
