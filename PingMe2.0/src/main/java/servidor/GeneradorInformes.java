package servidor;

import dao.UsuarioDAO;
import entidades.Usuario;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorInformes {
    
    public static byte[] generarInformeUsuarios() throws Exception {
        
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        
        
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("InformeUsuarios");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(515);
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);
        
        
        JRDesignField fieldId = new JRDesignField();
        fieldId.setName("id");
        fieldId.setValueClass(Long.class);
        jasperDesign.addField(fieldId);
        
        JRDesignField fieldUsername = new JRDesignField();
        fieldUsername.setName("username");
        fieldUsername.setValueClass(String.class);
        jasperDesign.addField(fieldUsername);
        
        JRDesignField fieldEmail = new JRDesignField();
        fieldEmail.setName("email");
        fieldEmail.setValueClass(String.class);
        jasperDesign.addField(fieldEmail);
        
        
        JRDesignBand bandaTitulo = new JRDesignBand();
        bandaTitulo.setHeight(50);
        
        JRDesignStaticText titulo = new JRDesignStaticText();
        titulo.setText("Informe de Usuarios - PingMe");
        titulo.setX(0);
        titulo.setY(10);
        titulo.setWidth(515);
        titulo.setHeight(30);
        titulo.setFontSize(18f);
        titulo.setBold(true);
        titulo.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        bandaTitulo.addElement(titulo);
        
        jasperDesign.setTitle(bandaTitulo);
        
        
        JRDesignBand bandaEncabezado = new JRDesignBand();
        bandaEncabezado.setHeight(30);
        
        JRDesignStaticText encabezadoId = new JRDesignStaticText();
        encabezadoId.setText("ID");
        encabezadoId.setX(0);
        encabezadoId.setY(0);
        encabezadoId.setWidth(50);
        encabezadoId.setHeight(20);
        encabezadoId.setBold(true);
        bandaEncabezado.addElement(encabezadoId);
        
        JRDesignStaticText encabezadoUsername = new JRDesignStaticText();
        encabezadoUsername.setText("Usuario");
        encabezadoUsername.setX(60);
        encabezadoUsername.setY(0);
        encabezadoUsername.setWidth(200);
        encabezadoUsername.setHeight(20);
        encabezadoUsername.setBold(true);
        bandaEncabezado.addElement(encabezadoUsername);
        
        JRDesignStaticText encabezadoEmail = new JRDesignStaticText();
        encabezadoEmail.setText("Email");
        encabezadoEmail.setX(270);
        encabezadoEmail.setY(0);
        encabezadoEmail.setWidth(245);
        encabezadoEmail.setHeight(20);
        encabezadoEmail.setBold(true);
        bandaEncabezado.addElement(encabezadoEmail);
        
        jasperDesign.setColumnHeader(bandaEncabezado);
        
        
        JRDesignBand bandaDetalle = new JRDesignBand();
        bandaDetalle.setHeight(25);
        
        JRDesignTextField campoId = new JRDesignTextField();
        campoId.setX(0);
        campoId.setY(0);
        campoId.setWidth(50);
        campoId.setHeight(20);
        JRDesignExpression expId = new JRDesignExpression();
        expId.setText("$F{id}");
        campoId.setExpression(expId);
        bandaDetalle.addElement(campoId);
        
        JRDesignTextField campoUsername = new JRDesignTextField();
        campoUsername.setX(60);
        campoUsername.setY(0);
        campoUsername.setWidth(200);
        campoUsername.setHeight(20);
        JRDesignExpression expUsername = new JRDesignExpression();
        expUsername.setText("$F{username}");
        campoUsername.setExpression(expUsername);
        bandaDetalle.addElement(campoUsername);
        
        JRDesignTextField campoEmail = new JRDesignTextField();
        campoEmail.setX(270);
        campoEmail.setY(0);
        campoEmail.setWidth(245);
        campoEmail.setHeight(20);
        JRDesignExpression expEmail = new JRDesignExpression();
        expEmail.setText("$F{email}");
        campoEmail.setExpression(expEmail);
        bandaDetalle.addElement(campoEmail);
        
        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(bandaDetalle);
        
        
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
        
        
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(usuarios);
        
        
        Map<String, Object> parametros = new HashMap<>();
        
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
        
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
        
        System.out.println("Informe PDF generado: " + outputStream.size() + " bytes");
        
        return outputStream.toByteArray();
    }
}
