package nl.rug.search.cpptool.gui;

import nl.rug.search.cpptool.api.*;
import nl.rug.search.cpptool.api.data.*;
import nl.rug.search.cpptool.api.io.Assembler;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JTreeGUI extends JFrame {

    private JPanel panel1;
    private JTree tree1;
    private JTable table;
    private JPanel main_panel;
    private JSplitPane splitpanel_1;
    private JSplitPane splitpanel_2;
    private JTextArea textArea;


    private String workingDir = "";
    private List<Node> classes;
    private DeclContainer result;
    private Long DSC;
    private Integer NOH;
    private String[] columnNames = {"Class", "ANA", "DAM", "DCC", "CAM", "MOA", "MFA", "NOP", "CIS", "NOM", "Effectiveness", "Extendibility", "Flexibility", "Functionality", "Reusability", "Understandability"};


    public JTreeGUI(String inputDir) {
        super("Tree Structure");
        setContentPane(main_panel);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        redirectSystemStreams();
    }

    private void createUIComponents() {
        createMenuBar();
        createTree();
        createTable(columnNames);
    }

    private void initProject(String dir) {
        workingDir = dir;
        textArea.setText("");

        importCpptoolFiles();
        calculateMetrics();

        initTree();
        initTable();
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea.append(text);
            }
        });
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void initTree() {
        insertDeclaration(result.context(), 0, (MutableTreeNode) tree1.getModel().getRoot());
    }


    private void initTable() {
        Object[][] data = new Object[classes.size()][16];

        //to form objects according to classes
        Integer i = 0;
        for (Node c : classes) {
            data[i][0] = c.dec.name().orElse("UNKNOWN");
            data[i][1] = c.ANA;
            data[i][2] = c.DAM;
            data[i][3] = c.DCC;
            data[i][4] = c.CAM;
            data[i][5] = c.MOA;
            data[i][6] = c.MFA;
            data[i][7] = c.NOP;
            data[i][8] = c.CIS;
            data[i][9] = c.NOM;
            data[i][10] = c.Effectiveness;
            data[i][11] = c.Extendibility;
            data[i][12] = c.Flexibility;
            data[i][13] = c.Functionality;
            data[i][14] = c.Reusability;
            data[i][15] = c.Understandability;
            i++;
        }
        table.setModel(new DefaultTableModel(data, columnNames));
    }


    private void createMenuBar() {
        //File option
        JMenuBar menu_bar = new JMenuBar();
        JMenu file = new JMenu("File");
        setJMenuBar(menu_bar);
        menu_bar.add(file);
        JMenuItem open = new JMenuItem("Open");
        file.add(open);

        class OpenAction implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                try {
                    PickFile();
                } catch (Exception a) {
                    a.printStackTrace();
                }
            }
        }
        open.addActionListener(new OpenAction());
    }



    private void importCpptoolFiles() {
        final Assembler assembler = Assembler.create();
        for (File f : new File(workingDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pb");
            }
        })) {
            assembler.read(f);
        }

        try {
            this.result = assembler.build();
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(this, "Couldn't load files from folder " + workingDir, "Cpptool-GUI", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void calculateMetrics() {
        classes = new ArrayList<>();
        Metrics.buildClasses(classes, result);
        DSC = Metrics.DSC(classes);
        NOH = Metrics.NOH(classes);
        Metrics.ANA(classes);
        Metrics.DAM(classes);
        Metrics.DCC(classes);
        Metrics.CAM(classes);
        Metrics.MOA(classes);
        Metrics.MFA(classes);
        Metrics.NOP(classes);
        Metrics.CIS(classes);
        Metrics.NOM(classes);
        Metrics.QMOOD(classes, DSC, NOH);
    }



    private void createTree() {
        MutableTreeNode top = new DeclarationMutableTreeNode("My Tree"); //top
        tree1 = new JTree(top);
    }


    /**
     * creates table
     * @param columnNames
     */
    private void createTable(String[] columnNames) {
        Object[][] data = new Object[0][16];

        table = new JTable(data, columnNames);
        table.setPreferredScrollableViewportSize(new Dimension(500, 50));
        table.setFillsViewportHeight(true);
    }

    int count = 0;
    MutableTreeNode old_node = null;
    MutableTreeNode old_node_2 = null;

    /**
     * to form tree
     * @param dc
     * @param index
     * @param top
     */
    private void insertDeclaration(DeclContext dc, int index, MutableTreeNode top) {
        // Create a tree node
        int c_index = 0;
        MutableTreeNode node;

        if (count == 0) {
            node = new DeclarationMutableTreeNode(getString(dc));
            top.insert(node, index);
        } else {
            node = new DeclarationMutableTreeNode(getString(dc));
            top.insert(node, index);
        }

        if (dc.definition().isPresent() && dc.definition().get().has(CxxRecord.class) && dc.definition().get().data(CxxRecord.class).isPresent()) {
            CxxRecord cxx = dc.definition().get().data(CxxRecord.class).get();
            for (CxxRecordParent p : cxx.parents()) {
                MutableTreeNode c_node = new DeclarationMutableTreeNode("(Parent) <" + p.access().toString().toLowerCase() + ">" + p.type().name());
                node.insert(c_node, c_index++);
            }
        }

        for (Declaration d : dc.declarations()) {
            boolean contain = false;
            for (DeclContext c_dc : dc.children()) {
                if (c_dc.definition().isPresent() && c_dc.definition().get().equals(d)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                MutableTreeNode c_node = new DeclarationMutableTreeNode(getString(d));
                node.insert(c_node, c_index++);
            }
        }

        for (DeclContext d : dc.children()) {
            count++;
            old_node = node;

            insertDeclaration(d, c_index++, node);
        }
    }


    /**
     * to convert declaration contexts into string
     * @param dc
     * @return
     */
    private String getString(DeclContext dc) {
        if (dc.definition().isPresent()) {
            return getString(dc.definition().get());
        } else {
            if (dc.name().toString() == "Optional.empty")
                return " (Namespace) ";
            else
                return " (Namespace) " + dc.name().toString().substring(9, dc.name().toString().length() - 1);
        }
    }

    /**
     * to convert declarations into string
     * @param d
     * @return
     */
    private String getString(Declaration d) {

        if (d.has(CxxRecord.class)) {

            return " [Class] " + d.name().get() + " ";
        } else if (d.has(CxxFunction.class)) {

            return " [Method] " + d.name().get() + " ";
        } else if (d.has(Function.class)) {

            return " [Function] " + d.name().get() + " ";
        } else if (d.declarationType() == DeclType.VARIABLE) {
            String varKind = "Variable";
            if (d.data(Variable.class).isPresent())
                varKind = d.data(Variable.class).get().kind().toString().toLowerCase();
            varKind = Character.toString(varKind.charAt(0)).toUpperCase() + varKind.substring(1);


            return " (" + varKind + ") " + d.name().get() + " ";
        } else if (d.declarationType() == DeclType.ENUM) {
            return " [Enum] " + d.name().get() + " ";
        }
        return d.toString();
    }


    /**
     * to select file using JFileChooser
     * @throws Exception
     */
    public void PickFile() throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            //get the file
            java.io.File file = chooser.getSelectedFile();
            initProject(file.getCanonicalPath());
        } else {
            System.exit(0);
        }
    }

}
