import re

with open("src/simrskhanza/DlgKamarInap.java", "r") as f:
    content = f.read()

action_code = """
    private void MnAsesmenInvasifNonBedahActionPerformed(java.awt.event.ActionEvent evt) {                                                      
       if(tabMode.getRowCount()==0){
            javax.swing.JOptionPane.showMessageDialog(null,"Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        }else{
            if(tbKamIn.getSelectedRow()>-1){
                this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                rekammedis.RMAsesmenInvasifNonBedah form=new rekammedis.RMAsesmenInvasifNonBedah(null,false);
                form.isCek();
                form.emptTeks();
                form.setNoRm(tbKamIn.getValueAt(tbKamIn.getSelectedRow(),5).toString(), new java.util.Date());
                form.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                form.setLocationRelativeTo(internalFrame1);
                form.setVisible(true);
                this.setCursor(java.awt.Cursor.getDefaultCursor());
            }else{
                javax.swing.JOptionPane.showMessageDialog(null,"Silahkan pilih data pada tabel...!!!!");
            }
        }
    }
"""

if "private void MnAsesmenInvasifNonBedahActionPerformed" not in content:
    # Just append it before the final brace
    content = re.sub(r'\}\s*$', action_code + '\n}', content)

with open("src/simrskhanza/DlgKamarInap.java", "w") as f:
    f.write(content)
