
package frontend;

import entidades.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.UsuarioService;

public class RegistroController {

	@FXML
	private TextField emailField;
	@FXML
	private TextField usuarioField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField edadField;
	@FXML
	private ComboBox<String> sexoCombo;
	@FXML
	private Button registrarBtn;
	@FXML
	private ComboBox<String> pregunta1Combo;
	@FXML
	private TextField respuesta1Field;

	@FXML
	public void initialize() {
		pregunta1Combo.getItems().addAll("¿Cuál es el nombre de tu primera mascota?", "¿En qué ciudad naciste?",
				"¿Cuál es el segundo nombre de tu madre o padre?", "¿Cuál fue el nombre de tu primer colegio?",
				"¿Cómo se llamaba tu profesor favorito de la infancia?");
	}

	@FXML
	private void onRegistrar(ActionEvent event) {

		String email = safeTxt(emailField);
		String usuario = safeTxt(usuarioField);
		String pass = safeTxt(passwordField);
		String edadTxt = safeTxt(edadField);
		String sexo = sexoCombo.getValue();
		String preguntaSeguridad1 = pregunta1Combo.getValue();
		String respuesta1 = safeTxt(respuesta1Field);

		// ==========================
		// Validaciones básicas
		// ==========================

		if (email.isEmpty() || usuario.isEmpty() || pass.isEmpty() || respuesta1.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Campos obligatorios",
					"Email, usuario, contraseña y las preguntas son obligatorios.");
			return;
		}

		if (!email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z]{2,}$")) {
			showAlert(Alert.AlertType.WARNING, "Email inválido", "Introduce un email con formato válido.");
			return;
		}

		// Validación numérica de edad (opcional)
		Integer edad = null;

		if (!edadTxt.isEmpty()) {
			try {
				edad = Integer.parseInt(edadTxt);

				if (edad < 0 || edad > 120) {
					showAlert(Alert.AlertType.WARNING, "Edad inválida", "Introduce una edad entre 0 y 120.");
					return;
				}

			} catch (NumberFormatException ex) {
				showAlert(Alert.AlertType.WARNING, "Edad inválida", "La edad debe ser un número.");
				return;
			}
		}

		try {
			UsuarioService usuarioService = new UsuarioService();

			Usuario u = new Usuario(usuario);
			u.setEmail(email);
			u.setContraseña(pass); 
			u.setSexo(sexo != null ? sexo : "");
			u.setEdad(edad != null ? edad : 21);
			u.setPregunta(preguntaSeguridad1);
			u.setRespuesta(respuesta1);

			usuarioService.crearUsuario(u);

			showAlert(Alert.AlertType.INFORMATION, "Registro completado",
					"Usuario registrado correctamente. Ya puedes iniciar sesión.");

			// Cerrar ventana modal
			Stage stage = (Stage) registrarBtn.getScene().getWindow();
			stage.close();

		} catch (IllegalStateException dup) {
			// Email duplicado
			showAlert(Alert.AlertType.WARNING, "Email duplicado", dup.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "No se pudo registrar el usuario.\n" + ex.getMessage());
		}
	}

	/*
	 * // ========================== // Regla de roles por dominio //
	 * ==========================
	 * 
	 * boolean isAdmin = email.toLowerCase().endsWith("@pingme.com") ||
	 * email.toLowerCase().endsWith("@pingme.net");
	 * 
	 * // ========================== // Simulación de guardado //
	 * ==========================
	 * 
	 * showAlert(Alert.AlertType.INFORMATION, "Registro completado",
	 * "Usuario registrado correctamente.\n" + (isAdmin ?
	 * "Se te asignará rol ADMIN por dominio corporativo." :
	 * "Se te asignará rol de usuario estándar."));
	 * 
	 * // Cerrar ventana modal Stage stage = (Stage)
	 * registrarBtn.getScene().getWindow(); stage.close();
	 */
	

	// ------------------------------------------
	// UTILIDADES
	// ------------------------------------------

	private String safeTxt(TextInputControl c) {
		return c.getText() != null ? c.getText().trim() : "";
	}

	private void showAlert(Alert.AlertType type, String header, String message) {
		Alert alert = new Alert(type);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
