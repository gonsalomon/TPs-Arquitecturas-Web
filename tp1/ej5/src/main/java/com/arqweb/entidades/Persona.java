package main.java.com.arqweb.entidades;

import javax.persistence.*;

@Entity
@Table(name = "Persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idPersona;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    private int edad;

    public Persona() {}

    public Persona(String nombre, String apellido, int edad) {
        this.nombre   = nombre;
        this.apellido = apellido;
        this.edad     = edad;
    }

    public int    getIdPersona() { return idPersona; }
    public String getNombre()    { return nombre; }
    public String getApellido()  { return apellido; }
    public int    getEdad()      { return edad; }

    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }
    public void setNombre(String nombre)    { this.nombre = nombre; }
    public void setApellido(String apellido){ this.apellido = apellido; }
    public void setEdad(int edad)           { this.edad = edad; }

    @Override
    public String toString() {
        return idPersona + " | " + nombre + " " + apellido + " | edad: " + edad;
    }
}