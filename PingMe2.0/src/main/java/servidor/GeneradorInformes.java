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
    
	// Genera un informe PDF con la lista de usuarios registrados en el sistema
	// El informe se basa en un archivo JRXML que define el diseño del reporte
    public static byte[] generarInformeUsuarios() throws Exception {
    	
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> usuarios = usuarioDAO.listarTodos();

        // Carga el archivo JRXML desde el classpath
        InputStream jrxmlStream = GeneradorInformes.class.getResourceAsStream(RUTA_JRXML);
        if (jrxmlStream == null) {
            throw new FileNotFoundException("No se encontró el archivo JRXML: " + RUTA_JRXML);
        }

        // Compila el archivo JRXML para obtener un objeto JasperReport
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        // Crea un JRBeanCollectionDataSource a partir de la lista de usuarios para alimentar el reporte
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(usuarios);

        // Define los parámetros que se pasarán al reporte. En este caso, solo un título para el informe.
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO_INFORME", "Informe de Usuarios - PingMe"); 

        // Llena el reporte con los datos y parámetros, obteniendo un objeto JasperPrint que representa el informe listo para exportar
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

        // Exporta el JasperPrint a un ByteArrayOutputStream en formato PDF
        // El tamaño del informe generado se muestra en la consola para verificar que se ha creado correctamente
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        System.out.println("Informe PDF generado: " + outputStream.size() + " bytes");

        return outputStream.toByteArray();
    }
}
