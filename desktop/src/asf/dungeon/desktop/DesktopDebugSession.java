package asf.dungeon.desktop;

import asf.dungeon.DungeonApp;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.TokenComponent;
import asf.dungeon.utility.UtDebugPrint;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Danny on 12/3/2014.
 */
public class DesktopDebugSession {
        private JFrame frame;
        ScheduledExecutorService exec;
        final DungeonApp app;

        public DesktopDebugSession(DungeonApp dungeonApp) {
                app = dungeonApp;

                EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                                createFrame();
                                showWindow();
                                exec =Executors.newSingleThreadScheduledExecutor();
                                exec.scheduleAtFixedRate(new Runnable() {
                                        @Override
                                        public void run() {
                                                Gdx.app.postRunnable(postRunnable);
                                        }
                                },1000,15, TimeUnit.MILLISECONDS);
                        }
                });
        }


        private final Runnable postRunnable = new Runnable() {
                @Override
                public void run() {
                        // search for valid instance of Dungeon, then call
                        // TODO: app.dungeonWorld used to be private, dungeonworld.dungeon used to be protected i kind of want to move back to those permission levels
                        if(app.dungeonWorld != null && app.dungeonWorld.dungeon != null){
                                updateDebugInfo(app.dungeonWorld.dungeon);
                        }
                }
        };



        public void showWindow(){
                EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                frame.setVisible(true);
                                rfBuildTree = true;
                        }
                });

        }

        private static GraphicsDevice getSecondaryGraphicsDevice() {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreenDevice = ge.getDefaultScreenDevice();
                GraphicsDevice[] gd = ge.getScreenDevices();
                if (gd.length < 1) return null;

                for (int i = 0; i < gd.length; i++) {
                        if (gd[i] != defaultScreenDevice) {
                                return gd[i];
                        }
                }
                return null;
        }

        private Object selectedTreeObject;
        private java.awt.Font fontInterface, fontText;
        private JCheckBox autoRefreshText;
        private boolean rfBuildTree;

        private void createFrame() {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                                try {UIManager.setLookAndFeel(info.getClassName()); }catch (Throwable ignored) {}
                                break;
                        }
                }

                fontInterface = new java.awt.Font("Ubuntu", 0, 18);
                fontText = new java.awt.Font("Courier New", 0, 20);

                frame = new JFrame("Dungeon Debug Session");
                frame.setFont(fontInterface);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                GraphicsDevice secondaryGraphicsDevice = getSecondaryGraphicsDevice();
                if (secondaryGraphicsDevice != null) {
                        frame.setLocation(secondaryGraphicsDevice.getDefaultConfiguration().getBounds().x, frame.getY());
                        //secondaryGraphicsDevice.setFullScreenWindow(frame);
                }
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                JPanel westPanel = new JPanel();
                westPanel.setLayout(new BorderLayout());
                JPanel southWestPanel = new JPanel();
                southWestPanel.setLayout(new BorderLayout());
                JPanel southWestSuicidePanel = new JPanel();
                southWestSuicidePanel.setLayout(new BorderLayout());

                JScrollPane scrollPane = new JScrollPane();
                JTree tree = new JTree();
                tree.setFont(fontInterface);
                tree.setDragEnabled(false);
                tree.setLargeModel(true);

                scrollPane.setViewportView(tree);

                JButton refreshButton = new JButton("Refresh Tree");
                refreshButton.setFont(fontInterface);
                refreshButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                rfBuildTree = true;
                        }

                });
                JButton refreshTextButton = new JButton("<>");
                refreshTextButton.setFont(fontInterface);
                autoRefreshText = new JCheckBox();
                autoRefreshText.setFont(fontInterface);
                autoRefreshText.setSelected(true);
                autoRefreshText.setToolTipText("Auto Refresh Text");

                southWestSuicidePanel.add(refreshTextButton, BorderLayout.LINE_START);
                southWestSuicidePanel.add(autoRefreshText, BorderLayout.LINE_END);

                southWestPanel.add(refreshButton, BorderLayout.LINE_START);
                southWestPanel.add(southWestSuicidePanel, BorderLayout.LINE_END);

                westPanel.add(scrollPane, BorderLayout.CENTER);
                westPanel.add(southWestPanel, BorderLayout.PAGE_END);

                JTabbedPane tabPane = new JTabbedPane();
                tabPane.setFont(fontInterface);

                frame.getContentPane().add(westPanel, BorderLayout.LINE_START);
                frame.getContentPane().add(tabPane, BorderLayout.CENTER);


                tree.addTreeSelectionListener(new TreeSelectionListener() {
                        public void valueChanged(TreeSelectionEvent evt) {
                                DungeonTreeNode treeNode = (DungeonTreeNode) evt.getPath().getLastPathComponent();
                                selectedTreeObject = treeNode.getUserObject();
                        }

                });

                rfBuildTree = true;

                frame.pack();

        }

        private void rebuildTreeModel(Dungeon dungeon) {
                if (frame == null) {
                        return;
                }

                JPanel westPanel = (JPanel) frame.getContentPane().getComponent(0);
                JScrollPane scrollPane = (JScrollPane) westPanel.getComponent(0);
                JTree tree = (JTree) scrollPane.getViewport().getView();

                autoSelectNode = null;
                currentFloorNode = null;
                DungeonTreeModel dungeonTreeModel = new DungeonTreeModel(build(dungeon));

                tree.setModel(dungeonTreeModel);
                expandAll(tree, true, dungeon.getCurrentFloopMap());



                if(autoSelectNode == null) autoSelectNode = currentFloorNode;
                if(autoSelectNode == null){
                        tree.setSelectionRow(0);
                }else{
                        TreePath tp = new TreePath(dungeonTreeModel.getPathToRoot(autoSelectNode));
                        tree.getSelectionModel().setSelectionPath(tp);
                }
        }

        private static void expandAll(JTree tree, boolean expand, FloorMap expandFm) {
                TreeNode root = (TreeNode) tree.getModel().getRoot();
                expandAll(tree, new TreePath(root), expand,expandFm);
        }

        private static void expandAll(JTree tree, TreePath parent, boolean expand, FloorMap expandFm) {
                TreeNode node = (TreeNode) parent.getLastPathComponent();
                if (node.getChildCount() >= 0) {
                        for (Enumeration e = node.children(); e.hasMoreElements();) {
                                DungeonTreeNode n = (DungeonTreeNode) e.nextElement();

                                Object o = n.getUserObject();
                                if (o instanceof FloorMap) {
                                        FloorMap fm = (FloorMap) o;
                                        if(fm != expandFm)
                                                return;
                                }


                                TreePath path = parent.pathByAddingChild(n);
                                expandAll(tree, path, expand, expandFm);
                        }
                }

                if (expand) {
                        tree.expandPath(parent);
                } else {
                        tree.collapsePath(parent);
                }
        }

        private DungeonTreeNode autoSelectNode;
        private DungeonTreeNode currentFloorNode;
        private final Array<Token> store = new Array<Token>(true, 32, Token.class);

        private  DungeonTreeNode build(Object o) {
                DungeonTreeNode treeNode = new DungeonTreeNode(o);
                if(o==selectedTreeObject){
                        autoSelectNode = treeNode;
                }
                if (o instanceof Dungeon) {
                        Dungeon dungeon = (Dungeon) o;
                        for(int i=0; i < dungeon.numFloormaps(); i++){
                                FloorMap fm = dungeon.generateFloor(i);
                                treeNode.add(build(fm));
                        }
                } else if (o instanceof FloorMap) {
                        FloorMap floorMap = (FloorMap) o;
                        if(currentFloorNode == null){
                                currentFloorNode = treeNode;
                        }
                        floorMap.getTokensOnFloor(store);
                        for (Token token : store) {
                                treeNode.add(build(token));

                        }
                }

                return treeNode;

        }


        private void setTabs(List<List<String>> tabContents){
                if (frame == null) {
                        return;
                }
                List<String> tabNames = new LinkedList<String>();
                for (List<String> tabContent : tabContents) {
                        String tabName = tabContent.get(0);
                        tabNames.add(tabName);
                        setTab(tabName, tabContent);
                }


                JTabbedPane tabPane = (JTabbedPane) frame.getContentPane().getComponent(1);
                for (int i = 0; i < tabPane.getTabCount(); i++) {
                        String titleAt = tabPane.getTitleAt(i);
                        if(!tabNames.contains(titleAt)){
                                tabPane.removeTabAt(i);
                                i = -1;
                        }
                }
        }

        private void removeTab(String tabName) {
                if (frame == null) {
                        return;
                }
                JTabbedPane tabPane = (JTabbedPane) frame.getContentPane().getComponent(1);
                int tabCount = tabPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                        String titleAt = tabPane.getTitleAt(i);
                        if (titleAt.equals(tabName)) {
                                tabPane.removeTabAt(i);
                                return;
                        }
                }
        }
        private static final boolean keepNullTab = true;

        private void setTab(String tabName, List<String> contents) {
                if (frame == null) {
                        return;
                }
                if (contents == null && keepNullTab) {
                        contents = new LinkedList<String>();
                }
                JTabbedPane tabPane = (JTabbedPane) frame.getContentPane().getComponent(1);
                JTextArea textArea = null;

                // find if this tab already exists
                int tabCount = tabPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                        String titleAt = tabPane.getTitleAt(i);
                        if (titleAt.equals(tabName)) {
                                if (contents != null) {
                                        JScrollPane scrollPane = (JScrollPane) tabPane.getComponentAt(i);
                                        textArea = (JTextArea) scrollPane.getViewport().getView();
                                        break;
                                } else {
                                        tabPane.removeTabAt(i);
                                        return;
                                }
                        }
                }

                if (contents == null) {
                        return;
                }

                // create this tab if it doesnt exist
                if (textArea == null) {
                        JScrollPane scrollPane = new JScrollPane();
                        textArea = new JTextArea();
                        textArea.setColumns(20);
                        textArea.setFont(fontText);
                        textArea.setRows(5);
                        textArea.setEditable(false);
                        scrollPane.setViewportView(textArea);

                        tabPane.addTab(tabName, scrollPane);
                }

                // add contents to the text area
                textArea.setText("");
                for (String contentLine : contents) {
                        if (textArea.getText().isEmpty()) {
                                textArea.append(contentLine);
                        } else {
                                textArea.append("\n" + contentLine);
                        }
                }

        }

        public void updateDebugInfo(final Dungeon dungeon) {
                EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                if(frame != null && frame.isVisible() && autoRefreshText.isSelected())
                                        refresh(dungeon);
                        }
                });



        }

        List<List<String>> tabs = new LinkedList<List<String>>();

        private void refresh(Dungeon dungeon){
                // TODO: accessing dungeon and building its output info needs to happen on the
                // game thread instead of the swing thread. As it is now dungeon is being accessed
                // concurrently but it is not thread safe.
                if(rfBuildTree || true) {
                        rebuildTreeModel(dungeon);
                        rfBuildTree = false;
                }
                if (selectedTreeObject == null) {
                        return;
                }

                tabs.clear();
                if (selectedTreeObject instanceof Dungeon) {
                        Dungeon d = (Dungeon) selectedTreeObject;
                        tabs.add(UtDebugPrint.getDebugInfo(d));
                        tabs.add(UtDebugPrint.getDebugInfo(d.getMasterJournal()));
                }else if(selectedTreeObject instanceof FloorMap){
                        FloorMap fm = (FloorMap) selectedTreeObject;

                        tabs.add(UtDebugPrint.getDebugInfo(fm));

                }else if (selectedTreeObject instanceof Token) {
                        Token token = (Token) selectedTreeObject;

                        tabs.add(UtDebugPrint.getDebugInfo(token));
                        Array<TokenComponent> components = token.components;
                        for (int i = 0; i < components.size; i++) {
                                TokenComponent component = components.get(i);
                                tabs.add(UtDebugPrint.getDebugInfo(component));
                        }
                }else{
                        tabs.add(new LinkedList<String>());
                }
                setTabs(tabs);
        }



        private static class DungeonTreeModel extends DefaultTreeModel {

                public DungeonTreeModel(TreeNode root) {
                        super(root);
                }

                public DungeonTreeModel(TreeNode root, boolean asksAllowsChildren) {
                        super(root, asksAllowsChildren);
                }

                @Override
                public boolean isLeaf(Object node) {
                        return ((DungeonTreeNode) node).getUserObject() instanceof Token;
                }

        }

        private static class DungeonTreeNode extends DefaultMutableTreeNode {

                public DungeonTreeNode() {
                }

                public DungeonTreeNode(Object userObject) {
                        super(userObject);
                }

                public DungeonTreeNode(Object userObject, boolean allowsChildren) {
                        super(userObject, allowsChildren);
                }

                @Override
                public String toString() {
                        Object o = getUserObject();
                        if (o instanceof Dungeon) {
                                return o.getClass().getSimpleName() + " (Dungeon)";
                        } else if (o instanceof FloorMap) {
                                FloorMap floorMap = (FloorMap) o;
                                return floorMap.index + " (FloorMap)";
                        } else if (o instanceof Token) {
                                Token token = (Token) o;
                                return token.name;
                        }
                        return super.toString();
                }

        }
}
