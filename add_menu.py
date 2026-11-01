import re

with open("src/simrskhanza/DlgKamarInap.java", "r") as f:
    content = f.read()

# 1. Add variable declaration
if "MnAsesmenInvasifNonBedah" not in content:
    content = re.sub(
        r'(private javax.swing.JMenuItem MnPenilaianAwalKeperawatanRanap;)', 
        r'\1\n    private javax.swing.JMenuItem MnAsesmenInvasifNonBedah;', 
        content
    )

# 2. Add instantiation and properties
init_code = """
        MnAsesmenInvasifNonBedah = new javax.swing.JMenuItem();
        MnAsesmenInvasifNonBedah.setBackground(new java.awt.Color(255, 255, 254));
        MnAsesmenInvasifNonBedah.setFont(new java.awt.Font("Tahoma", 0, 11));
        MnAsesmenInvasifNonBedah.setForeground(new java.awt.Color(50, 50, 50));
        MnAsesmenInvasifNonBedah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png")));
        MnAsesmenInvasifNonBedah.setText("Asesmen Invasif Non Bedah");
        MnAsesmenInvasifNonBedah.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        MnAsesmenInvasifNonBedah.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        MnAsesmenInvasifNonBedah.setName("MnAsesmenInvasifNonBedah"); 
        MnAsesmenInvasifNonBedah.setPreferredSize(new java.awt.Dimension(240, 26));
        MnAsesmenInvasifNonBedah.addActionListener(this::MnAsesmenInvasifNonBedahActionPerformed);
"""
if "MnAsesmenInvasifNonBedah =" not in content:
    content = re.sub(
        r'(MnPenilaianAwalKeperawatanRanapBayiAnak\.addActionListener.*?;\n)', 
        r'\1' + init_code, 
        content
    )

# 3. Add to MnPenilaianAwal
if "MnPenilaianAwal.add(MnAsesmenInvasifNonBedah);" not in content:
    content = re.sub(
        r'(MnPenilaianAwal\.add\(MnPenilaianAwalKeperawatanRanapBayiAnak\);)', 
        r'\1\n        MnPenilaianAwal.add(MnAsesmenInvasifNonBedah);', 
        content
    )

# 4. Add ActionPerformed method
action_code = """
    private void MnAsesmenInvasifNonBedahActionPerformed(java.awt.event.ActionEvent evt) {                                                      
       if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbKamIn.getSelectedRow()>-1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                rekammedis.RMAsesmenInvasifNonBedah form=new rekammedis.RMAsesmenInvasifNonBedah(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(tbKamIn.getValueAt(tbKamIn.getSelectedRow(),5).toString(), new java.util.Date());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Silahkan pilih data pada tabel...!!!!");
            }
        }
    }
"""
if "MnAsesmenInvasifNonBedahActionPerformed" not in content:
    content = re.sub(
        r'(private void MnPenilaianAwalKeperawatanRanapBayiAnakActionPerformed.*?^    \}\n)', 
        r'\1' + action_code, 
        content,
        flags=re.MULTILINE|re.DOTALL
    )

with open("src/simrskhanza/DlgKamarInap.java", "w") as f:
    f.write(content)

