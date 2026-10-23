package inventory;

import inventory.RencanaKebutuhanObatHitung.Baris;
import java.io.File;
import java.util.List;
import java.util.Map;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/** Export Rencana Kebutuhan Obat & BMHP ke .xls (angka tetap numerik) */
public final class RencanaKebutuhanObatExcel {
    private RencanaKebutuhanObatExcel(){}

    public static void tulis(File file,String judul,String periode,Map<String,Object> judulKolom,
                             Map<String,List<Baris>> data,List<Map<String,?>> barisCetak) throws Exception {
        WritableWorkbook wb=Workbook.createWorkbook(file);
        WritableSheet sh=wb.createSheet("Rencana Kebutuhan",0);

        WritableFont fTebal=new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD);
        WritableFont fBiasa=new WritableFont(WritableFont.ARIAL,9);
        WritableCellFormat cJudul=new WritableCellFormat(new WritableFont(WritableFont.ARIAL,12,WritableFont.BOLD));
        WritableCellFormat cHead=new WritableCellFormat(fTebal);
        cHead.setWrap(true);cHead.setAlignment(Alignment.CENTRE);cHead.setVerticalAlignment(VerticalAlignment.CENTRE);
        cHead.setBorder(Border.ALL,BorderLineStyle.THIN);cHead.setBackground(Colour.LIGHT_GREEN);
        WritableCellFormat cKel=new WritableCellFormat(fTebal);
        cKel.setBorder(Border.ALL,BorderLineStyle.THIN);cKel.setBackground(Colour.PALE_BLUE);
        WritableCellFormat cTeks=new WritableCellFormat(fBiasa);cTeks.setBorder(Border.ALL,BorderLineStyle.THIN);
        WritableCellFormat cAngka=new WritableCellFormat(fBiasa,new NumberFormat("#,##0.##"));cAngka.setBorder(Border.ALL,BorderLineStyle.THIN);
        WritableCellFormat cRp=new WritableCellFormat(fBiasa,new NumberFormat("#,##0"));cRp.setBorder(Border.ALL,BorderLineStyle.THIN);
        WritableCellFormat cPersen=new WritableCellFormat(fBiasa,new NumberFormat("0.00%"));cPersen.setBorder(Border.ALL,BorderLineStyle.THIN);
        WritableCellFormat cTotal=new WritableCellFormat(fTebal,new NumberFormat("#,##0"));cTotal.setBorder(Border.ALL,BorderLineStyle.THIN);cTotal.setBackground(Colour.VERY_LIGHT_YELLOW);
        WritableCellFormat cTotalTeks=new WritableCellFormat(fTebal);cTotalTeks.setBorder(Border.ALL,BorderLineStyle.THIN);cTotalTeks.setBackground(Colour.VERY_LIGHT_YELLOW);

        sh.addCell(new Label(0,0,judul,cJudul));
        sh.addCell(new Label(0,1,"PERIODE "+periode,cJudul));
        String[] head={"KODE BARANG SIM RS","NO","NAMA GENERIK","NAMA OBAT MERK DAGANG","SATUAN","SISA STOK",
            (String)judulKolom.get("judul_g"),(String)judulKolom.get("judul_h"),(String)judulKolom.get("judul_i"),(String)judulKolom.get("judul_j"),
            "RENCANA KEBUTUHAN","RENCANA PENGADAAN","HARGA SATUAN","JUMLAH TOTAL HARGA","% ITEM","% KUMULATIF","PARETO","VEN","KET."};
        String[] huruf={"","","","","","F","G","H","I","J","K = H+I+J-F","L","M","N = L x M","O","P","Q","",""};
        int[] lebar={14,5,32,32,9,10,14,14,12,12,12,12,12,16,8,9,7,6,24};
        for(int c=0;c<head.length;c++){
            sh.addCell(new Label(c,3,head[c],cHead));
            sh.addCell(new Label(c,4,huruf[c],cHead));
            sh.setColumnView(c,lebar[c]);
        }
        sh.setRowView(3,900);
        int r=5;
        for(Map.Entry<String,List<Baris>> e:data.entrySet()){
            if(e.getValue().isEmpty()) continue;
            sh.addCell(new Label(0,r,"",cKel));sh.addCell(new Label(1,r,"",cKel));
            sh.addCell(new Label(2,r,e.getKey(),cKel));
            for(int c=3;c<head.length;c++) sh.addCell(new Label(c,r,"",cKel));
            r++;
            int no=1;
            double total=0;
            for(Baris b:e.getValue()){
                sh.addCell(new Label(0,r,b.kode,cTeks));
                sh.addCell(new jxl.write.Number(1,r,no++,cAngka));
                sh.addCell(new Label(2,r,b.generik,cTeks));
                sh.addCell(new Label(3,r,b.nama,cTeks));
                sh.addCell(new Label(4,r,b.satuan,cTeks));
                double[] v={b.f,b.g,b.h,b.i,b.j,b.k,b.l};
                for(int c=0;c<v.length;c++) sh.addCell(new jxl.write.Number(5+c,r,v[c],cAngka));
                sh.addCell(new jxl.write.Number(12,r,b.m,cRp));
                sh.addCell(new jxl.write.Number(13,r,b.n,cRp));
                sh.addCell(new jxl.write.Number(14,r,b.o,cPersen));
                sh.addCell(new jxl.write.Number(15,r,b.p,cPersen));
                sh.addCell(new Label(16,r,b.pareto,cTeks));
                sh.addCell(new Label(17,r,b.ven,cTeks));
                sh.addCell(new Label(18,r,b.ket,cTeks));
                total+=b.n;
                r++;
            }
            for(int c=0;c<head.length;c++) sh.addCell(new Label(c,r,c==12?"TOTAL":"",cTotalTeks));
            sh.addCell(new jxl.write.Number(13,r,total,cTotal));
            r+=2;
        }
        // usulan baru
        boolean adaUsulan=false;
        int no=1;
        double totalUsulan=0;
        for(Map<String,?> m:barisCetak){
            if(!"USULAN".equals(m.get("jenis"))) continue;
            if(!adaUsulan){
                sh.addCell(new Label(2,r,"USULAN OBAT/BMHP BARU",cKel));
                r++;
                String[] hu={"NO","NAMA OBAT/BMHP","KELOMPOK","SATUAN","RENCANA PENGADAAN","HARGA","TOTAL","KETERANGAN"};
                for(int c=0;c<hu.length;c++) sh.addCell(new Label(1+c,r,hu[c],cHead));
                r++;
                adaUsulan=true;
            }
            sh.addCell(new jxl.write.Number(1,r,no++,cAngka));
            sh.addCell(new Label(2,r,(String)m.get("generik"),cTeks));
            sh.addCell(new Label(3,r,(String)m.get("nama"),cTeks));
            sh.addCell(new Label(4,r,(String)m.get("satuan"),cTeks));
            sh.addCell(new jxl.write.Number(5,r,(Double)m.get("l"),cAngka));
            sh.addCell(new jxl.write.Number(6,r,(Double)m.get("m"),cRp));
            sh.addCell(new jxl.write.Number(7,r,(Double)m.get("n"),cRp));
            sh.addCell(new Label(8,r,(String)m.get("ket"),cTeks));
            totalUsulan+=(Double)m.get("n");
            r++;
        }
        if(adaUsulan){
            sh.addCell(new Label(6,r,"TOTAL",cTotalTeks));
            sh.addCell(new jxl.write.Number(7,r,totalUsulan,cTotal));
        }
        sh.getSettings().setVerticalFreeze(5);
        wb.write();
        wb.close();
    }
}
