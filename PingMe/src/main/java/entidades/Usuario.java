package entidades;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Usuario implements Serializable{
	private String id;
	private String nombre;
	private String contraseña;
	private String email;
	private String sexo;
	private int edad;

	public Usuario(String id, String nombre, String contraseña, String email, String sexo, int edad) {
		this.id = id;
		this.nombre = nombre;
		this.contraseña = contraseña;
		this.email = email;
		this.sexo = sexo;
		this.edad = edad;
	}

	public Usuario(String nombre) {
		this.id = "";
		this.nombre = nombre;
		this.contraseña = "";
		this.sexo = "";
		this.edad = 21;
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", sexo=" + sexo + ", edad=" + edad + "]";
	}
	
	
}
