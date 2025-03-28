    package com.github.iamsxm.allformat.ui;

import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.github.iamsxm.allformat.cache.CacheName;
import com.github.iamsxm.allformat.cache.ParamCache;
import com.github.iamsxm.allformat.component.HexConvertPanel;
import com.github.iamsxm.allformat.component.ImageLabel;
import com.github.iamsxm.allformat.event.TextPanelMouseListener;
import com.github.iamsxm.allformat.util.*;
import com.google.common.io.BaseEncoding;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;

public class NewDialog extends JFrame {
    private final static String CODE_GITHUB_URL = "https://github.com/iamsxm/all-format";
    private JTabbedPane tabbedPane1;
    private JPanel contentPane;
    private JButton exeBtn;
    private JTextPane qrcodeText;
    private JLabel msgLabel;
    private JTextArea base64Text;
    private JCheckBox topCheckBox;
    private JButton otherBtn;
    private JCheckBox newLineCheckBox;
    private JTextArea encodeText;

    private JPanel jsPanel;
    private JPanel xmlPanel;
    private JPanel htmlPanel;
    private JPanel sqlPanel;
    private JScrollPane qrcodePanel;
    private JScrollPane base64Panel;
    private JScrollPane encodePanel;
    private HexConvertPanel hexConvertPanel1;
    private JButton urlDecodeBtn;
    private JButton urlEncodeBtn;
    private JButton md5Btn;

    private RSyntaxTextArea jsonText;
    private RSyntaxTextArea xmlText;
    private RSyntaxTextArea htmlText;
    private RSyntaxTextArea sqlText;

    private static final String JSON = "JSON";
    private static final String XML = "XML";
    private static final String HTML = "HTML";
    private static final String SQL = "SQL";
    private static final String QRCODE = "QRCode";
    private static final String Base64 = "Base64";
    private static final String ENCODE = "Encode";
    private static final String HEX_CONVERT = "HexConvert";
    private static final String TRANSLATE = "Translate";
    private static final boolean TRAN_FLAG = false;
    private boolean isDarcula = false;

    private ParamCache pc;

    private TextPanelMouseListener tpml = null;

    public NewDialog(boolean isDarcula) {
        this.isDarcula = isDarcula;

        pc = new ParamCache();

        setContentPane(contentPane);
        getRootPane().setDefaultButton(exeBtn);

        initComponent();

        initActionListener();

        showMainDia();

        setClipboardContent();
    }

    private void initComponent() {
        encodeText.setOpaque(false);
        base64Text.setOpaque(false);
        qrcodeText.setOpaque(false);
        otherBtn.setText("压缩");
        otherBtn.setVisible(true);

        urlEncodeBtn.setVisible(false);
        urlDecodeBtn.setVisible(false);
        md5Btn.setVisible(false);

        createRSyntaxTextArea();

        if (isDarcula) {
            tabbedPane1.setForeground(new Color(213, 212, 212));
        }
    }

    private void showMainDia() {
        this.setLocationRelativeTo(null);
        this.setTitle("AllFormat");
        this.initCacheParam();
        this.setSize(800, 460);
        this.setMinimumSize(new Dimension(700, 430));
        // 获取鼠标当前位置（通常与IDE窗口位置一致）
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point mousePoint = pointerInfo.getLocation();

        // 获取所有显示器设备
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        // 查找包含鼠标位置的显示器
        GraphicsDevice targetScreen = null;
        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            if (screenBounds.contains(mousePoint)) {
                targetScreen = screen;
                break;
            }
        }

        if (targetScreen != null) {
            // 在目标显示器居中
            Rectangle bounds = targetScreen.getDefaultConfiguration().getBounds();
            int x = bounds.x + (bounds.width - this.getWidth()) / 2;
            int y = bounds.y + (bounds.height - this.getHeight()) / 2;
            this.setLocation(x, y);
        } else {
            // 回退到默认显示器逻辑
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - this.getWidth()) / 2;
            int y = (screenSize.height - this.getHeight()) / 2;
            this.setLocation(x, y);
        }
        this.setVisible(true);

        xmlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        htmlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        sqlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        jsonText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonText.requestFocus();
        jsonText.grabFocus();
    }

    private void initCacheParam() {
        Boolean onTopPar = pc.readAsBoolean(CacheName.ON_TOP);
        if (null == onTopPar || onTopPar) {
            this.topCheckBox.setSelected(true);
            this.setAlwaysOnTop(true);
        } else {
            this.topCheckBox.setSelected(false);
            this.setAlwaysOnTop(false);
        }

        Boolean newLinePar = pc.readAsBoolean(CacheName.NEW_LINE);
        this.newLineCheckBox.setSelected(null != newLinePar && newLinePar);
    }

    private void hideMainDia() {
        this.setVisible(false);
        pc.writeByName(CacheName.NEW_LINE, Boolean.toString(newLineCheckBox.isSelected()));
        pc.writeByName(CacheName.ON_TOP, Boolean.toString(isAlwaysOnTop()));
        pc.close();
    }

    private void setClipboardContent() {//http://weasdfasdfa.sdfadsf.com
        String clipText = ClipboardUtil.getSysClipboardText();
        if (null == clipText || clipText.isEmpty()) {
            try {
                tabbedPane1.setSelectedIndex(4);
                ClipboardUtil.pasteClipboardContent(qrcodeText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            clipText = clipText.trim();
            if (clipText.startsWith("http:") || clipText.startsWith("https:")) {
                tabbedPane1.setSelectedIndex(6);
                encodeText.setText(clipText);
            } else {
                jsonText.setText(clipText);
            }
        }
    }

    private void initActionListener() {
        exeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tag = tabbedPane1.getTitleAt(tabbedPane1.getSelectedIndex());
                switch (tag.trim()) {
                    case JSON:
                        jsonOK();
                        break;
                    case XML:
                        xmlOK();
                        break;
                    case HTML:
                        htmlOK();
                        break;
                    case QRCODE:
                        qrCodeOK();
                        break;
                    case Base64:
                        encode();
                        break;
                    case SQL:
                        sqlFormat();
                        break;
                    case ENCODE:
                        zhToUnicode();
                        break;
                    case HEX_CONVERT:
                        hexConvertPanel1.setValue();
                        break;
                }
            }
        });
        topCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectTop(topCheckBox.isSelected());
            }
        });
        newLineCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (newLineCheckBox.isSelected()) {
                    jsonText.setLineWrap(true);
                    xmlText.setLineWrap(true);
                    htmlText.setLineWrap(true);
                    sqlText.setLineWrap(true);
                    base64Text.setLineWrap(true);
                    encodeText.setLineWrap(true);
                } else {
                    jsonText.setLineWrap(false);
                    xmlText.setLineWrap(false);
                    htmlText.setLineWrap(false);
                    sqlText.setLineWrap(false);
                    base64Text.setLineWrap(false);
                    encodeText.setLineWrap(false);
                }
            }
        });
        otherBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tag = tabbedPane1.getTitleAt(tabbedPane1.getSelectedIndex()).trim();
                if (JSON.equalsIgnoreCase(tag)) {
                    jsonCompress();
                } else if (Base64.equalsIgnoreCase(tag)) {
                    decode();
                } else if (ENCODE.equalsIgnoreCase(tag)) {
                    unicodeToZh();
                } else if (QRCODE.equalsIgnoreCase(tag)) {
                    decodeQrcode();
                }
            }
        });
        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                String tag = tabbedPane1.getTitleAt(pane.getSelectedIndex()).trim();
                otherBtn.setVisible(false);
                newLineCheckBox.setVisible(true);
                msgLabel.setForeground(Color.BLACK);
                msgLabel.setText("\u70b9\u51fb\u6309\u94ae\u8fdb\u884c\u683c\u5f0f\u5316");
                exeBtn.setText("\u683c\u5f0f\u5316");
                urlEncodeBtn.setVisible(false);
                urlDecodeBtn.setVisible(false);
                md5Btn.setVisible(false);
                if (JSON.equalsIgnoreCase(tag)) {
                    otherBtn.setText("\u538b\u7f29");
                    otherBtn.setVisible(true);
                } else if (Base64.equalsIgnoreCase(tag)) {
                    base64Text.requestFocus();
                    base64Text.grabFocus();
                    otherBtn.setText("\u89e3\u5bc6");
                    otherBtn.setVisible(true);
                    exeBtn.setText("\u52a0\u5bc6");
                } else if (QRCODE.equalsIgnoreCase(tag)) {
                    exeBtn.setText("\u751f\u6210");
                    otherBtn.setText("\u89e3\u6790");
                    otherBtn.setVisible(true);
                    qrcodeText.requestFocus();
                    qrcodeText.grabFocus();
                } else if (SQL.equalsIgnoreCase(tag)) {
                    exeBtn.setText("\u7f8e\u5316");
                    sqlText.requestFocus();
                    sqlText.grabFocus();
                } else if (ENCODE.equalsIgnoreCase(tag)) {
                    exeBtn.setText("\u4e2d\u8f6c\u0055");
                    otherBtn.setText("\u0055\u8f6c\u4e2d");
                    otherBtn.setVisible(true);
                    urlEncodeBtn.setVisible(true);
                    urlDecodeBtn.setVisible(true);
                    md5Btn.setVisible(true);
                    encodeText.requestFocus();
                    encodeText.grabFocus();
                } else if (HEX_CONVERT.equalsIgnoreCase(tag)) {
                    hexConvertPanel1.setFocus();
                    newLineCheckBox.setVisible(false);
                    exeBtn.setText("\u8f6c\u6362");
                }
            }
        });

        md5Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                md5OK();
            }
        });
        urlEncodeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = encodeText.getText();
                if (null == text || "".equalsIgnoreCase(text)) {
                    return;
                }
                encodeText.setText(URLUtil.encode(text));
                msgLabel.setText("url encode success!");
            }
        });

        urlDecodeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = encodeText.getText();
                if (null == text || "".equalsIgnoreCase(text)) {
                    return;
                }
                encodeText.setText(URLUtil.decode(text));
                msgLabel.setText("url decode success!");
            }
        });

        tpml = new TextPanelMouseListener(tabbedPane1);
        qrcodeText.addMouseListener(tpml);
        base64Text.addMouseListener(tpml);
        encodeText.addMouseListener(tpml);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                hideMainDia();
                dispose();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideMainDia();
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void selectTop(boolean flag) {
        this.setAlwaysOnTop(flag);
    }

    private void jsonOK() {
        String text = jsonText.getText();
        if (null == text || "".equals(text)) {
            return;
        }

        text = text.replaceAll("\t", "");
        String resStr = null;
        try {
            resStr = FormatUtil.format(text);
            msgLabel.setText("json format!");
            jsonText.setText(resStr);
        } catch (Exception e) {
            resStr = MapFormat.format(text);
            msgLabel.setText("map format!");
            jsonText.setText(resStr);
        }
        jsonText.setCaretPosition(0);
    }

    private void jsonCompress() {
        jsonOK();
        String text = jsonText.getText();
        if (null == text || "".equals(text)) {
            return;
        }
        jsonText.setText(JSONUtil.parse(text).toString());
        msgLabel.setText("json compress!");
    }

    private void xmlOK() {
        String text = xmlText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }

        text = text.replaceAll("\t", "");
        try {
            String resStr = XmlFormat.format(text);
            msgLabel.setText("xml format success!");
            xmlText.setText(resStr);
        } catch (Throwable e) {
            String eStr = "xml format error [" + e.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void htmlOK() {
        String text = htmlText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }

        text = text.replaceAll("\t", "");
        try {
            String resStr = HtmlFormat.format(text);
            msgLabel.setText("html format!");
            htmlText.setText(resStr);
        } catch (Throwable e) {
            String eStr = "html format error [" + e.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void md5OK() {
        String text = encodeText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        try {
            text = MD5Util.md5(text).toUpperCase(Locale.ROOT);
            msgLabel.setText("md5 success!");
            encodeText.setText(text);
        } catch (Throwable t) {
            String eStr = "md5 error [" + t.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void qrCodeOK() {
        String text = qrcodeText.getText();
        if (null == text || "".equalsIgnoreCase(text.trim())) {
            return;
        }

        try {
            qrcodeText.setText(text.trim() + "\r\n");
            qrcodeText.setCaretPosition(qrcodeText.getStyledDocument().getLength());
            BufferedImage bufferedImage = QrCodeCreateUtil.createQrCode(text.trim(), 250);
            ImageIcon ii = new ImageIcon(bufferedImage);
            ImageLabel label = new ImageLabel(qrcodeText, ii);
            qrcodeText.insertComponent(label);

            msgLabel.setText("qrcode create!");
        } catch (Exception e) {
            String eStr = "qrcode create exception [" + e.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void decodeQrcode() {
        try {
            String results = "";
            for (int i = 0; i < qrcodeText.getDocument().getLength(); i++) {
                Element elem = ((StyledDocument) qrcodeText.getDocument()).getCharacterElement(i);
                AttributeSet as = elem.getAttributes();
                if (as.containsAttribute(AbstractDocument.ElementNameAttribute, StyleConstants.ComponentElementName)) {
                    if (StyleConstants.getComponent(as) instanceof JLabel) {
                        ImageLabel myLabel = (ImageLabel) StyleConstants.getComponent(as);
                        ImageIcon imageIcon = myLabel.getImageIcon();
                        results += QrCodeCreateUtil.decode(imageIcon.getImage()) + "\r\n";
                    }
                }
            }
            if (results.isEmpty()) {
                msgLabel.setText("未解析到图片内容");
                msgLabel.setToolTipText(msgLabel.getText());
            } else {
                qrcodeText.setCaretPosition(qrcodeText.getStyledDocument().getLength());
                qrcodeText.setText(results);
            }
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("未解析到图片内容");
            msgLabel.setToolTipText(msgLabel.getText());
        }
    }

    private void encode() {
        String text = base64Text.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        try {
            text = BaseEncoding.base64().encode(text.getBytes());
            base64Text.setText(text);
            msgLabel.setText("base64 encode!");
            msgLabel.setToolTipText("base64 encode!");
        } catch (Exception e) {
            String eStr = "base64 encode exception [" + e.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void decode() {
        String text = base64Text.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        try {
            text = new String(BaseEncoding.base64().decode(text));
            base64Text.setText(text);
            msgLabel.setText("base64 decode!");
            msgLabel.setToolTipText("base64 decode!");
        } catch (Exception e) {
            String eStr = "base64 decode exception [" + e.getMessage() + "]";
            msgLabel.setText(eStr);
            msgLabel.setToolTipText(eStr);
        }
    }

    private void sqlFormat() {
        String text = sqlText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        text = SqlFormat.format(text);
        if (null == text) {
            msgLabel.setText("sql format error!");
            return;
        }
        sqlText.setText(text);
        msgLabel.setText("sql format!");
        msgLabel.setToolTipText("sql format!");
    }

    private void zhToUnicode() {
        String text = encodeText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        String result = UnicodeUtil.unicodeEncode(text);
        if (null == result) {
            msgLabel.setText("unicode encode error!");
            return;
        }
        encodeText.setText(result);
        msgLabel.setText("unicode encode!");
        msgLabel.setToolTipText("unicode encode!");
    }

    private void unicodeToZh() {
        String text = encodeText.getText();
        if (null == text || "".equalsIgnoreCase(text)) {
            return;
        }
        String result = UnicodeUtil.unicodeDecode(text);
        if (null == result) {
            msgLabel.setText("unicode decode error!");
            return;
        }
        encodeText.setText(result);
        msgLabel.setText("unicode decode!");
        msgLabel.setToolTipText("unicode decode!");
    }

    private void createRSyntaxTextArea() {
        jsonText = createArea(JSON);
        RTextScrollPane jsonSp = new RTextScrollPane(jsonText);
        jsonSp.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));

        xmlText = createArea(XML);
        RTextScrollPane xmlSp = new RTextScrollPane(xmlText);
        xmlSp.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));

        htmlText = createArea(HTML);
        RTextScrollPane htmlSp = new RTextScrollPane(htmlText);
        htmlSp.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));

        sqlText = createArea(SQL);
        RTextScrollPane sqlSp = new RTextScrollPane(sqlText);
        sqlSp.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));

        jsPanel.add(jsonSp);
        xmlPanel.add(xmlSp);
        htmlPanel.add(htmlSp);
        sqlPanel.add(sqlSp);
    }

    private RSyntaxTextArea createArea(String type) {
        RSyntaxTextArea area = new RSyntaxTextArea();
        area.setDocument(new MaxLengthDocument(5000000));
        if (JSON.equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        } else if (XML.equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        } else if (HTML.equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        } else if (SQL.equalsIgnoreCase(type)) {
            area.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        }
        area.setCodeFoldingEnabled(true);
        area.setAntiAliasingEnabled(true);
        area.setAutoscrolls(true);
        if (true == isDarcula) {
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(area);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SyntaxScheme scheme = area.getSyntaxScheme();
            scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = Color.BLUE;
            scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = new Color(164, 0, 0);
            scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = new Color(164, 0, 0);
            scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.RED;
            scheme.getStyle(Token.OPERATOR).foreground = Color.BLACK;
            area.revalidate();
        }
        return area;
    }

    public class MaxLengthDocument extends RSyntaxDocument {
        int maxChars;

        public MaxLengthDocument(int max) {
            super(SYNTAX_STYLE_NONE);
            maxChars = max;
        }

        @Override
        public void insertString(int offset, String s, AttributeSet a) throws BadLocationException {
            try {
                if (getLength() + s.length() > maxChars) {
                    Toolkit.getDefaultToolkit().beep();
                    tipDia("内容过长，最大" + maxChars + "个字符!");
                    return;
                }
                super.insertString(offset, s, a);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void tipDia(String msg) {
        JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.WARNING_MESSAGE);
    }
}
