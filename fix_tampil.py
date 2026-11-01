import re

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "r") as f:
    content = f.read()

# Replace tampil() method completely
tampil_code = """
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            String query = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,reg_periksa.umurdaftar,reg_periksa.sttsumur,asesmen_keperawatan_invasif_non_bedah.nip,"+
                           "petugas.nama,asesmen_keperawatan_invasif_non_bedah.tanggal,asesmen_keperawatan_invasif_non_bedah.jam_tiba_rr,asesmen_keperawatan_invasif_non_bedah.lokasi_pungsi,"+
                           "asesmen_keperawatan_invasif_non_bedah.jumlah_pungsi,asesmen_keperawatan_invasif_non_bedah.terpasang_sheath,asesmen_keperawatan_invasif_non_bedah.lokasi_sheath,"+
                           "asesmen_keperawatan_invasif_non_bedah.pulsasi_radialis_kanan,asesmen_keperawatan_invasif_non_bedah.pulsasi_radialis_kiri,asesmen_keperawatan_invasif_non_bedah.pulsasi_dorsalis_kanan,"+
                           "asesmen_keperawatan_invasif_non_bedah.pulsasi_dorsalis_kiri,asesmen_keperawatan_invasif_non_bedah.alat_terpasang,asesmen_keperawatan_invasif_non_bedah.jenis_anestesi,"+
                           "asesmen_keperawatan_invasif_non_bedah.sedasi,asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_pre,asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_intra,"+
                           "asesmen_keperawatan_invasif_non_bedah.antikoagulan_dosis_post,asesmen_keperawatan_invasif_non_bedah.komplikasi,asesmen_keperawatan_invasif_non_bedah.balance_cairan_intake,"+
                           "asesmen_keperawatan_invasif_non_bedah.balance_cairan_output,asesmen_keperawatan_invasif_non_bedah.instruksi_paska_tindakan,asesmen_keperawatan_invasif_non_bedah.masalah_keperawatan,"+
                           "asesmen_keperawatan_invasif_non_bedah.rencana_keperawatan,asesmen_keperawatan_invasif_non_bedah.keputusan_rawat "+
                           "from asesmen_keperawatan_invasif_non_bedah inner join reg_periksa on asesmen_keperawatan_invasif_non_bedah.no_rawat=reg_periksa.no_rawat "+
                           "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join petugas on asesmen_keperawatan_invasif_non_bedah.nip=petugas.nip "+
                           "where asesmen_keperawatan_invasif_non_bedah.tanggal between ? and ? "+
                           (TCari.getText().trim().equals("") ? "" : "and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or asesmen_keperawatan_invasif_non_bedah.nip like ? or petugas.nama like ?) ") +
                           "order by asesmen_keperawatan_invasif_non_bedah.tanggal";
                           
            ps=koneksi.prepareStatement(query);
            try {
                ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                if(!TCari.getText().trim().equals("")){
                    ps.setString(3,"%"+TCari.getText().trim()+"%");
                    ps.setString(4,"%"+TCari.getText().trim()+"%");
                    ps.setString(5,"%"+TCari.getText().trim()+"%");
                    ps.setString(6,"%"+TCari.getText().trim()+"%");
                    ps.setString(7,"%"+TCari.getText().trim()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getString("tgl_lahir"),rs.getString("umurdaftar")+" "+rs.getString("sttsumur"),
                        rs.getString("nip"),rs.getString("nama"),rs.getString("tanggal"),rs.getString("jam_tiba_rr"),rs.getString("lokasi_pungsi"),rs.getString("jumlah_pungsi"),
                        rs.getString("terpasang_sheath"),rs.getString("lokasi_sheath"),rs.getString("pulsasi_radialis_kanan"),rs.getString("pulsasi_radialis_kiri"),
                        rs.getString("pulsasi_dorsalis_kanan"),rs.getString("pulsasi_dorsalis_kiri"),rs.getString("alat_terpasang"),rs.getString("jenis_anestesi"),
                        rs.getString("sedasi"),rs.getString("antikoagulan_dosis_pre"),rs.getString("antikoagulan_dosis_intra"),rs.getString("antikoagulan_dosis_post"),
                        rs.getString("komplikasi"),rs.getString("balance_cairan_intake"),rs.getString("balance_cairan_output"),rs.getString("instruksi_paska_tindakan"),
                        rs.getString("masalah_keperawatan"),rs.getString("rencana_keperawatan"),rs.getString("keputusan_rawat")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }
"""
content = re.sub(r'private void tampil\(\) \{.*?\n        LCount.setText\(""\+tabMode.getRowCount\(\)\);\n    \}', tampil_code, content, flags=re.DOTALL)

getData_code = """
    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            JamTiba.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            LokasiPungsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            JumlahPungsi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            TerpasangSheath.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            LokasiSheath.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            PulsasiRadialisKanan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            PulsasiRadialisKiri.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            PulsasiDorsalisKanan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            PulsasiDorsalisKiri.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            AlatTerpasang.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            JenisAnestesi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            Sedasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            DosisPre.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            DosisIntra.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            DosisPost.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            Komplikasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            Intake.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            Output.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            InstruksiPaska.setText(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString());
            MasalahKeperawatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString());
            RencanaKeperawatan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());
            KeputusanRawat.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());
            
            Valid.SetTgl(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
        }
    }
"""
content = re.sub(r'private void getData\(\) \{.*?\n    \}', getData_code, content, flags=re.DOTALL)


# Also need to fix the tbObat table columns since there are more columns now.
# Search for: tabMode=new DefaultTableModel(null,new Object[]{...});
tabMode_code = """
        tabMode=new DefaultTableModel(null,new Object[]{
            "No.Rawat","No.R.M.","Nama Pasien","Tgl.Lahir","Umur","NIP","Petugas","Tanggal","Jam Tiba","Lokasi Pungsi","Jml Pungsi","Tpsg Sheath",
            "Lokasi Sheath","Radialis Kanan","Radialis Kiri","Dorsalis Kanan","Dorsalis Kiri","Alat","Anestesi","Sedasi","Dosis Pre","Dosis Intra",
            "Dosis Post","Komplikasi","Intake","Output","Instruksi Paska","Masalah Kep","Rencana Kep","Keputusan Rawat"
        }){
"""
content = re.sub(r'tabMode=new DefaultTableModel\(null,new Object\[\]\{\n.*?\n        \}\)\{', tabMode_code, content, flags=re.DOTALL)

with open("src/rekammedis/RMAsesmenInvasifNonBedah.java", "w") as f:
    f.write(content)

