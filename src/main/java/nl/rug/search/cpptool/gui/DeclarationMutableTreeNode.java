package nl.rug.search.cpptool.gui;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by begumbenel on 19.7.2015.
 */
public class DeclarationMutableTreeNode extends DefaultMutableTreeNode {

    private String declName;

    public DeclarationMutableTreeNode(String name) {
        declName = name;
    }

    @Override
    public String toString() {
        return declName;
    }
}
