package com.example.proyect.model;


public abstract class Usuario implements Autenticable {

    private String nombre;
    private String correo;
    private final String documento;
    private String telefono;
    private final String usuario;
    private final String contrasena;
    private boolean activo;

    public Usuario(String nombre, String documento, String correo, String telefono,
                   String usuario, String contrasena) {
        validar(nombre, "El nombre");
        validar(documento, "El documento");
        validar(correo, "El correo");
        validar(telefono, "El teléfono");
        validar(usuario, "El usuario");
        validar(contrasena, "La contraseña");
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.telefono = telefono;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.activo = true;
    }

    private static void validar(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
    }

    @Override
    public boolean autenticar(String usuario, String contrasena) {
        return activo && this.usuario.equals(usuario) && this.contrasena.equals(contrasena);
    }

    public void actualizarDatos(String nombre, String correo, String telefono) {
        validar(nombre, "El nombre");
        validar(correo, "El correo");
        validar(telefono, "El teléfono");
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
    }

    public void desactivar() {
        this.activo = false;
    }

    @Override
    public abstract Rol getRol();

    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getUsuario() { return usuario; }
    public boolean isActivo() { return activo; }
}
