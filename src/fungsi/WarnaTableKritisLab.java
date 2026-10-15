package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer tabel hasil laboratorium 2 tingkat (gaya PROLIMS).
 *
 * <p>Kritis (batas dari tabel set_nilai_kritis_lab), warna MERAH tebal:</p>
 * <ul>
 *   <li>nilai &gt;= batas_atas   -&gt; KRITIS TINGGI, tanda "HH"</li>
 *   <li>nilai &lt;= batas_bawah  -&gt; KRITIS RENDAH, tanda "LL"</li>
 *   <li>hasil teks mengandung teks_kritis (Reaktif/Positif/+3/...) -&gt; tanda "!"</li>
 * </ul>
 *
 * <p>Di luar nilai rujukan tapi BELUM kritis (rentang dibaca dari kolom
 * Nilai Rujukan, mis. "4 - 12"), warna ORANYE:</p>
 * <ul>
 *   <li>nilai &gt; batas atas rujukan -&gt; tanda "H"</li>
 *   <li>nilai &lt; batas bawah rujukan -&gt; tanda "L"</li>
 * </ul>
 *
 * @author Khanza
 */
public class WarnaTableKritisLab extends DefaultTableCellRenderer {

    private final Map<String,Double> batasBawah;
    private final Map<String,Double> batasAtas;
    private final Map<String,String> teksKritis;
    private final int colHasil;
    private final int colIdTemplate;
    private final int colRujukan;

    private static final Pattern ANGKA = Pattern.compile("\\d+(?:[.,]\\d+)?");

    public WarnaTableKritisLab(Map<String,Double> batasBawah, Map<String,Double> batasAtas,
                               Map<String,String> teksKritis, int colHasil, int colIdTemplate, int colRujukan){
        this.batasBawah=batasBawah;
        this.batasAtas=batasAtas;
        this.teksKritis=teksKritis;
        this.colHasil=colHasil;
        this.colIdTemplate=colIdTemplate;
        this.colRujukan=colRujukan;
    }

    /** Ambil angka dari teks hasil (buang satuan/simbol, koma -&gt; titik). */
    private static Double parseNum(String s){
        if(s==null){ return null; }
        String t=s.trim().replace(",", ".");
        // hanya bilangan bersih; hasil spt "(+)3/500", "> 500", "Negatif" TIDAK dinilai
        if(!t.matches("-?\\d+(?:\\.\\d+)?")){ return null; }
        try { return Double.valueOf(t); } catch(Exception e){ return null; }
    }

    /**
     * Parse rentang nilai rujukan -&gt; [bawah, atas]; Double.NaN bila tak ada.
     * Dukung "4 - 12", "3.9 - 5.3", "&lt;= 50" (hanya atas), "&gt;= 5" (hanya bawah).
     */
    private static double[] parseRentang(String s){
        double bawah=Double.NaN, atas=Double.NaN;
        if(s==null){ return new double[]{bawah,atas}; }
        String t=s.trim().replace(",", ".");
        if(t.isEmpty()){ return new double[]{bawah,atas}; }
        Matcher m=ANGKA.matcher(t);
        double a=Double.NaN, b=Double.NaN; int n=0;
        while(m.find()){
            double v;
            try { v=Double.parseDouble(m.group()); } catch(Exception e){ continue; }
            if(n==0){ a=v; } else if(n==1){ b=v; }
            n++;
        }
        if(n>=2){
            bawah=Math.min(a,b); atas=Math.max(a,b);
        }else if(n==1){
            boolean adaAtas = t.contains("≤") || t.contains("<");   // <= / <
            boolean adaBawah = t.contains("≥") || t.contains(">");  // >= / >
            if(adaAtas && !adaBawah){ atas=a; }
            else if(adaBawah && !adaAtas){ bawah=a; }
        }
        return new double[]{bawah,atas};
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column){
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // reset gaya ke default tiap render (renderer dipakai ulang)
        setFont(getFont().deriveFont(Font.PLAIN));
        setToolTipText(null);
        if(row % 2 == 1){
            c.setBackground(new Color(255,244,244));
        }else{
            c.setBackground(new Color(255,255,255));
        }
        c.setForeground(Color.BLACK);

        try {
            Object idObj = table.getValueAt(row, colIdTemplate);
            Object hasilObj = table.getValueAt(row, colHasil);
            String id = idObj==null ? "" : idObj.toString().trim();
            String hasil = hasilObj==null ? "" : hasilObj.toString().trim();

            if(!id.isEmpty() && !hasil.isEmpty()){
                boolean kritis=false;
                boolean luarRujukan=false;
                String tanda="";
                String jenis="";

                Double angka = parseNum(hasil);

                // --- 1) cek KRITIS (merah) ---
                if(angka!=null){
                    Double ba = batasAtas.get(id);
                    Double bb = batasBawah.get(id);
                    if(ba!=null && angka>=ba){
                        kritis=true; tanda="HH"; jenis=" (Tinggi)";
                    }else if(bb!=null && angka<=bb){
                        kritis=true; tanda="LL"; jenis=" (Rendah)";
                    }
                }
                if(!kritis && teksKritis!=null){
                    String tk = teksKritis.get(id);
                    if(tk!=null && !tk.isEmpty() && hasil.toLowerCase().contains(tk.toLowerCase())){
                        kritis=true; tanda="!";
                    }
                }

                // --- 2) kalau belum kritis, cek DI LUAR RUJUKAN (oranye) ---
                if(!kritis && angka!=null){
                    Object rjObj = table.getValueAt(row, colRujukan);
                    double[] r = parseRentang(rjObj==null ? "" : rjObj.toString());
                    if(!Double.isNaN(r[1]) && angka>r[1]){
                        luarRujukan=true; tanda="H"; jenis=" (Tinggi)";
                    }else if(!Double.isNaN(r[0]) && angka<r[0]){
                        luarRujukan=true; tanda="L"; jenis=" (Rendah)";
                    }
                }

                if(kritis){
                    c.setBackground(new Color(255,205,205));
                    c.setForeground(new Color(180,0,0));
                    setFont(getFont().deriveFont(Font.BOLD));
                    if(column==colHasil){ setText(hasil + "  " + tanda); }
                    setToolTipText("NILAI KRITIS" + jenis + " - segera laporkan ke DPJP/petugas ruangan");
                }else if(luarRujukan){
                    if("L".equals(tanda)){
                        c.setForeground(new Color(24,95,165));   // rendah = biru
                    }else{
                        c.setForeground(new Color(186,117,23));  // tinggi = oranye
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                    if(column==colHasil){ setText(hasil + "  " + tanda); }
                    setToolTipText("Di luar nilai rujukan" + jenis);
                }
            }
        } catch(Exception e){
            // biarkan tampil normal jika ada masalah baca sel
        }

        return c;
    }
}
