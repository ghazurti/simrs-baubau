import net.sf.jasperreports.engine.JasperCompileManager;

public class CompileReport {
    public static void main(String[] args) {
        try {
            System.out.println("Compiling " + args[0]);
            JasperCompileManager.compileReportToFile(args[0], args[1]);
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
