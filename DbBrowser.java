/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbbrowser;
import com.mysql.jdbc.Connection;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jarda
 */
public class DbBrowser extends javax.swing.JFrame {

    //deklarace proměnných
    private Connection spojeni;
    private JPanel vyber;
    private JComboBox dbs;
    private JComboBox tables;
    private JTable tabulka;
    private DefaultTableModel model;
    private JScrollPane scrollPane;
    private JButton csv;
    private JButton xml;
    private JButton json;
    private JPanel export;
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DbBrowser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DbBrowser().setVisible(true);
            }
        });
    }
    public DbBrowser()
    {
        this.setLayout(new BorderLayout());
        this.setSize(500, 500);
        vyber = new JPanel();
        vyber.setLayout(new GridLayout(1,2,0,0));
        dbs = new JComboBox();
        tables = new JComboBox();
        tabulka = null;
        vyber.add(dbs);
        vyber.add(tables);
        this.add(vyber,NORTH);
        dbs.addItem("Vyber databázi");
        tables.addItem("Vyber tabulku");
        initConnection();
        dbs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbConnection();
            }
        });
        tables.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                if(tables.getSelectedItem() != "Vyber tabulku")
                    tablesListener();
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        
        tabulka = new JTable();
        model = (DefaultTableModel) tabulka.getModel();
        tabulka.setAutoCreateRowSorter(true);
        scrollPane = new JScrollPane(tabulka);
        this.add(scrollPane,CENTER);
        csv = new JButton();
        xml = new JButton();
        json = new JButton();
        csv.setText("CSV");
        xml.setText("XML");
        json.setText("JSON");
        csv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCSVActionPerformed(evt);
            }
        });
        json.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuJSONActionPerformed(evt);
            }
        });
        xml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuXMLActionPerformed(evt);
            }
        });
        export = new JPanel();
        export.setLayout(new GridLayout(1,3,0,0));
        export.add(csv);
        export.add(xml);
        export.add(json);
        this.add(export,SOUTH);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        
    }
    //prvotní připojení k databázi
    private void initConnection() {
        try {
            this.spojeni = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=convertToNull", "root", "");
            DatabaseMetaData meta = spojeni.getMetaData();
            ResultSet res = meta.getCatalogs();
            while (res.next()) {
                    String db=res.getString("TABLE_CAT");
                    dbs.addItem(db);
            }
            dbs.setSelectedItem("Vyber databázi");
            res.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nedošlo k připojení databáze", "Chyba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    //připojení k databázi vybrané v dbs
    private void dbConnection()
    {
        try {
            if(dbs.getItemAt(0)== "Vyber databázi")
            {
                dbs.removeItemAt(0);
            }
            this.spojeni.close();
            tables.removeAllItems();
            tables.addItem("Vyber tabulku");
            this.spojeni = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/"+(String)dbs.getSelectedItem()+"?zeroDateTimeBehavior=convertToNull", "root", "");
            DatabaseMetaData md = spojeni.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
              tables.addItem(rs.getString(3));
            }
        } catch (SQLException ex) {
            //JOptionPane.showMessageDialog(this, "Nedošlo k připojení databáze", "Chyba", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    //metoda pro zpracování výběru tabulky z comboboxu tables
    private void tablesListener()
    {
        if(tables.getItemAt(0)== "Vyber tabulku")
        {
            tables.removeItemAt(0);
        }
        listData(getAllRecords());
    }
    
    /**
     * @return vraci obsah vybrané tabulky
     */
    private ResultSet getAllRecords() {
        ResultSet vysledky = null;
        try {
            PreparedStatement dotaz = this.spojeni.prepareStatement("SELECT * FROM "+tables.getSelectedItem());
            vysledky = dotaz.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Chyba při komunikaci s databází", "Chyba", JOptionPane.ERROR_MESSAGE);
        }
        return vysledky;
    }
    
    /**
     * vypíše data z tabulky v databázi do tabulky v aplikaci
     * @param data výsledek dotazu SELECT * FROM ***
     */
    private void listData(ResultSet data) {
        //vymazání tabulky
        if(tabulka != null)
        {
            for (int i = tabulka.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
            model.setColumnCount(0);
        }
        //naplnění tabulky
        try {
            int pocetSloupcu = data.getMetaData().getColumnCount();
            for(int i = 1; i <= pocetSloupcu; i++)
                model.addColumn(data.getMetaData().getColumnName(i));
            Object[] objekty = new Object[pocetSloupcu];
            while (data.next()) {
                for(int i = 0; i < pocetSloupcu; i++)
                    objekty[i] = data.getObject(i+1);
                model.addRow(objekty);
            }
            if (tabulka.getRowCount() > 0) {
                tabulka.setRowSelectionInterval(0, 0);
            } 
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Chyba při komunikaci s databází", "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * export do csv
     * @param evt 
     */
    private void menuCSVActionPerformed(java.awt.event.ActionEvent evt) {                                        
        try {
            /* Vytvoření a zobrazení dialogového okna pro uložení souboru */
            JFileChooser fc = new JFileChooser();
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setDialogTitle("Uložení souboru CSV");
            fc.setCurrentDirectory(new java.io.File("."));
            FileNameExtensionFilter myFilter = new FileNameExtensionFilter("CSV soubor", "csv");
            fc.setFileFilter(myFilter);
            String data = "";
            for(int i = 0; i < tabulka.getColumnCount(); i++)
            {
                if(i != 0)
                    data += ";";
                data += tabulka.getColumnName(i);
            }
            data+="\n";
            for (int i = 0; i < tabulka.getRowCount(); i++) {
                for(int j = 0; j < model.getColumnCount(); j++)
                {
                    data += tabulka.getModel().getValueAt(i, j).toString();
                    data += (j != model.getColumnCount()-1) ? ";" : "";
                }
                data += (i == tabulka.getRowCount()-1) ? "" : "\n";
            }
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.ulozDoSouboru(fc.getSelectedFile(), "Windows-1250", data);
            }
        } // Zachycení obecné výjimky
        catch (HeadlessException e) {
            // Zobrazení dialogového okna s upozorněním na chybu
            JOptionPane.showMessageDialog(this, "Nastala chyba při ukládání souboru!", "Chyba!", JOptionPane.ERROR_MESSAGE);
        }

    }                                       

    /**
     * export do JSON
     * @param evt 
     */
    private void menuJSONActionPerformed(java.awt.event.ActionEvent evt) {                                         
        try {
            /* Vytvoření a zobrazení dialogového okna pro uložení souboru */
            JFileChooser fc = new JFileChooser();
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setDialogTitle("Uložení souboru JSON");
            fc.setCurrentDirectory(new java.io.File("."));
            FileNameExtensionFilter myFilter = new FileNameExtensionFilter("JSON soubor", "json");
            fc.setFileFilter(myFilter);
            String data = "{\""+dbs.getSelectedItem()+"\": [\n";
            for (int i = 0; i < tabulka.getRowCount(); i++) {
                data +="\t{";
                for(int j = 0; j < model.getColumnCount(); j++)
                {
                    data+="\""+model.getColumnName(j)+"\":\""+ tabulka.getModel().getValueAt(i, j).toString() + "\"";
                    data += (j == model.getColumnCount()-1) ? "}" : ",";
                }
                data += (i == tabulka.getRowCount()-1) ? "\n" : ",\n";
            }
            data += "]}";
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.ulozDoSouboru(fc.getSelectedFile(), "UTF-8", data);
            }
        } // Zachycení obecné výjimky
        catch (HeadlessException e) {
            // Zobrazení dialogového okna s upozorněním na chybu
            JOptionPane.showMessageDialog(this, "Nastala chyba při ukládání souboru!", "Chyba!", JOptionPane.ERROR_MESSAGE);
        }
    }                                        

    /**
     * export do XML
     * @param evt 
     */
    private void menuXMLActionPerformed(java.awt.event.ActionEvent evt) {                                        
        try {
            /* Vytvoření a zobrazení dialogového okna pro uložení souboru */
            JFileChooser fc = new JFileChooser();
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setDialogTitle("Uložení souboru XML");
            fc.setCurrentDirectory(new java.io.File("."));
            FileNameExtensionFilter myFilter = new FileNameExtensionFilter("XML soubor", "xml");
            fc.setFileFilter(myFilter);
            String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<"+dbs.getSelectedItem()+">\n";
            for (int i = 0; i < tabulka.getRowCount(); i++) {
                data += "\t<zaznam>\n";
                for(int j = 0; j < model.getColumnCount(); j++)
                {
                    data += "\t\t<"+model.getColumnName(j)+">" + tabulka.getModel().getValueAt(i, j).toString() + "</"+model.getColumnName(j)+">\n";
                }
                data += "\t</zaznam>\n";
            }
            data += "</"+dbs.getSelectedItem()+">";
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.ulozDoSouboru(fc.getSelectedFile(), "UTF-8", data);
            }
        } // Zachycení obecné výjimky
        catch (HeadlessException e) {
            // Zobrazení dialogového okna s upozorněním na chybu
            JOptionPane.showMessageDialog(this, "Nastala chyba při ukládání souboru!", "Chyba!", JOptionPane.ERROR_MESSAGE);
        }
    } 

    public Boolean ulozDoSouboru(File soubor, String charset, String data) {
        try {
            /* Otevření proudu pro zápis do souboru */
            OutputStream outputStream = new FileOutputStream(soubor);
            /* Provedení zápisu dat do souboru v dané znakové sadě */
            try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
                writer.write(data);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
}
