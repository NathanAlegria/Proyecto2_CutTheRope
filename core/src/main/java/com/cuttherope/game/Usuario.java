package com.cuttherope.game;

import java.io.Serializable;
import java.util.Date;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreUsuario;
    private String contrasena;
    private String nombreCompleto;
    private Date fechaRegistro;
    private int nivelActual;
    private int cantidadPartidasJugadas;

    public Usuario() {
        this.fechaRegistro = new Date();
        this.nivelActual = 1;
        this.cantidadPartidasJugadas = 0;
    }

    public Usuario(String nombreUsuario, String contrasena, String nombreCompleto) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombreCompleto = nombreCompleto;
        this.fechaRegistro = new Date();
        this.nivelActual = 1;
        this.cantidadPartidasJugadas = 0;
    }

    public Usuario(String nombreUsuario, String contrasena, String nombreCompleto, Date fechaRegistro, int nivelActual, int cantidadPartidasJugadas) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombreCompleto = nombreCompleto;
        this.fechaRegistro = fechaRegistro;
        this.nivelActual = nivelActual;
        this.cantidadPartidasJugadas = cantidadPartidasJugadas;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public int getNivelActual() { return nivelActual; }
    public void setNivelActual(int nivelActual) { this.nivelActual = nivelActual; }

    public int getCantidadPartidasJugadas() { return cantidadPartidasJugadas; }
    public void setCantidadPartidasJugadas(int cantidadPartidasJugadas) { this.cantidadPartidasJugadas = cantidadPartidasJugadas; }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                ", nivelActual=" + nivelActual +
                ", cantidadPartidasJugadas=" + cantidadPartidasJugadas +
                '}';
    }
}
