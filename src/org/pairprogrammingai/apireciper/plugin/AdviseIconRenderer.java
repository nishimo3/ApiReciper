package org.pairprogrammingai.apireciper.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorGutter;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class AdviseIconRenderer extends GutterIconRenderer{
    public Icon getIcon(){
        return IconLoader.getIcon("/icons/" + iconIndex + ".png");
    }

    private String[] data;
    private int iconIndex = 0;
    private int lineNumber = 0;

    public AdviseIconRenderer(String advise, int _iconIndex, int _lineNumber){
        data = advise.split(System.getProperty("line.separator"));
        if(_iconIndex < 10) {
            iconIndex = _iconIndex;
        }
        lineNumber = _lineNumber;
    }

    /*
    public static RangeHighlighter createHighlighter(@NotNull MarkupModelEx markup) {
        final RangeHighlighterEx highlighter;
        int line = 20;
        if (line >= 0) {
            highlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
            if (highlighter != null) {
                highlighter.setGutterIconRenderer(new AdviseIconRenderer());
            }
        }else {
            highlighter = null;
        }
        return highlighter;
    }
    */

    public AnAction getClickAction() {
        return new DummyPopup();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public class DummyPopup extends AnAction {

        //private MessageType messageType;

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // TODO: insert action logic here

            Editor editor = e.getData(CommonDataKeys.EDITOR);
            EditorGutter gutter = editor.getGutter();
            CaretModel caret = editor.getCaretModel();

            Point point = editor.logicalPositionToXY(caret.getLogicalPosition());

            /*
            JBPopupFactory myJBPopupFactory = JBPopupFactory.getInstance();
            JBPopup myJBPopup = myJBPopupFactory.createMessage("Hello¥n 1¥n 2 ¥n");
            myJBPopup.setSize(new Dimension(200, 100));
            myJBPopup.show(new RelativePoint((Component)gutter,new Point(0,100)));
            */

            JList<String> myList=new JList<String>(data);
            JBPopupFactory.getInstance()
                    .createListPopupBuilder(myList)
                    .setTitle("Not Enough API Lists")
                    .createPopup()
                    .show(new RelativePoint((Component)gutter,new Point(-20 , editor.getLineHeight() * lineNumber)));

            /*
            JBPopupFactory.getInstance()
                    .createMessage("Hello" + System.getProperty("line.separator")+"x="
                            +point.x+System.getProperty("line.separator")
                            +"y="+point.y+System.getProperty("line.separator")
                            +"offset="+offset+System.getProperty("line.separator"))
                    .setSize(new Dimension(1000, 1000))
                    .show(new RelativePoint((Component)gutter,new Point(0,100)));
            */
            //.setLocation(point);
        }
    }
}