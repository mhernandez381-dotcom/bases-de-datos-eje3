package edu.huella.dto;

/**
 * Datos que el usuario puede escribir en el formulario: solo NIT y nombre.
 * El propietario, el ID y las fechas NO vienen del formulario (propietario = sesión).
 */
public record EmpresaFormulario(String nit, String nombre) {
}
