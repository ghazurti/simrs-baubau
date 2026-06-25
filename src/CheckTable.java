import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import fungsi.koneksiDB;

public class CheckTable {
    public static void main(String[] args) {
        try {
            Connection con = koneksiDB.condb();
            System.out.println("Current active database: " + con.getCatalog());
            Statement st = con.createStatement();
            System.out.println("Altering referensi_mobilejkn_bpjs tables...");
            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            
            // Alter sik schema (if not already completed)
            st.executeUpdate("ALTER TABLE sik.referensi_mobilejkn_bpjs MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
            st.executeUpdate("ALTER TABLE sik.referensi_mobilejkn_bpjs_batal MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
            
            // Alter sik_update schema
            try {
                st.executeUpdate("ALTER TABLE sik_update.referensi_mobilejkn_bpjs MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
                st.executeUpdate("ALTER TABLE sik_update.referensi_mobilejkn_bpjs_batal MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
                System.out.println("Alter sik_update completed successfully.");
            } catch (Exception ex) {
                System.out.println("Skipped or failed altering sik_update: " + ex.getMessage());
            }
            
            // Alter faga schema
            try {
                st.executeUpdate("ALTER TABLE faga.referensi_mobilejkn_bpjs MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
                st.executeUpdate("ALTER TABLE faga.referensi_mobilejkn_bpjs_batal MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
                System.out.println("Alter faga completed successfully.");
            } catch (Exception ex) {
                System.out.println("Skipped or failed altering faga: " + ex.getMessage());
            }

            // Alter referensi_addantrian_bpjs_onsite
            try {
                st.executeUpdate("ALTER TABLE sik.referensi_addantrian_bpjs_onsite MODIFY COLUMN nobooking VARCHAR(40) NOT NULL");
                System.out.println("Alter referensi_addantrian_bpjs_onsite completed successfully.");
            } catch (Exception ex) {
                System.out.println("Skipped or failed altering referensi_addantrian_bpjs_onsite: " + ex.getMessage());
            }

            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("Alter tables completed.");
            
            String[] tables = {"referensi_mobilejkn_bpjs_taskid", "mutasi_berkas", "referensi_mobilejkn_bpjs", "referensi_mobilejkn_bpjs_batal"};
            System.out.println("Checking actual column lengths...");
            ResultSet rsLen = st.executeQuery("select TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, CHARACTER_MAXIMUM_LENGTH from INFORMATION_SCHEMA.COLUMNS where COLUMN_NAME like '%booking%'");
            while (rsLen.next()) {
                System.out.println(rsLen.getString("TABLE_SCHEMA") + "." + rsLen.getString("TABLE_NAME") + "." + rsLen.getString("COLUMN_NAME") + " size: " + rsLen.getInt("CHARACTER_MAXIMUM_LENGTH"));
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
