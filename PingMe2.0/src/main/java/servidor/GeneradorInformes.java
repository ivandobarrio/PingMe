package servidor;

import dao.UsuarioDAO;
import entidades.Usuario;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorInformes {
	
	private static final String RUTA_JRXML = "/reportes/informe_usuarios.jrxml";
    
    public static byte[] generarInformeUsuarios() throws Exception {
        
    	
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> usuarios = usuarioDAO.listarTodos();

        
        InputStream jrxmlStream = GeneradorInformes.class.getResourceAsStream(RUTA_JRXML);
        if (jrxmlStream == null) {
            throw new FileNotFoundException("No se encontró el archivo JRXML: " + RUTA_JRXML);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(usuarios);

        
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO_INFORME", "Informe de Usuarios - PingMe"); 

        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        System.out.println("✓ Informe PDF generado: " + outputStream.size() + " bytes");

        return outputStream.toByteArray();
    }
}
