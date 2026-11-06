import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class PracticaGimnasio {

    private static final String URL      = "jdbc:mysql://localhost:3306/gimnasio";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "contrasena";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion = -1;

        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            Statement st = con.createStatement()) {
            System.out.println("Conexion OK");

            while (opcion != 0) {
                System.out.println("\n===== GIMNASIO =====");
                System.out.println("1. Listar actividades");
                System.out.println("2. Insertar socio");
                System.out.println("3. Cambiar ciudad de un socio");
                System.out.println("4. Borrar inscripciones de un socio");
                System.out.println("0. Salir");
                System.out.print("Opcion: ");

                try {
                    opcion = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Escribe un numero.");
                    continue;
                }

                try {
                    switch (opcion) {
                        case 1:
                            listarActividades(st);
                            break;
                        case 2:
                            System.out.print("Nombre: ");
                            String nombre = sc.nextLine();
                            System.out.print("Ciudad: ");
                            String ciudad = sc.nextLine();
                            System.out.print("Email: ");
                            String email = sc.nextLine();
                            insertarSocio(con, nombre, ciudad, email);
                            break;
                        case 3:
                            System.out.print("Id del socio: ");
                            int id = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Nueva ciudad: ");
                            String nuevaCiudad = sc.nextLine();
                            cambiarCiudad(con, id, nuevaCiudad);
                            break;
                        case 4:
                            System.out.print("Id del socio: ");
                            borrarInscripciones(con, Integer.parseInt(sc.nextLine().trim()));
                            break;
                        case 0:
                            System.out.println("Adios.");
                            break;
                        default:
                            System.out.println("Opcion no valida.");
                    }
                } catch (SQLException e) {
                    // si algo falla (dato incorrecto...) se avisa y se vuelve al menu
                    System.out.println("Error: " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("El id tiene que ser un numero.");
                }
            }

        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 1: muestra todas las actividades con un Statement: id - nombre - dia - precio
    //    (SELECT id_actividad, nombre, dia, precio FROM actividad).
    // ---------------------------------------------------------------
    static void listarActividades(Statement st) throws SQLException {
        String sql = "SELECT a.id_actividad, a.nombre, a.dia, m.nombre AS monitor FROM actividad a JOIN monitor m ON a.id_monitor = m.id_monitor";
        try (ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getInt("id_actividad") + " - " + rs.getString("nombre")
                        + " - " + rs.getString("dia") + " - " + rs.getString("monitor"));
            }
        }
    }

    // ---------------------------------------------------------------
    // 2: inserta el socio en la tabla socio (nombre, ciudad, email, fecha_alta)
    //    con un PreparedStatement (3 "?") y la fecha de alta de hoy,
    //    y muestra las filas afectadas.
    //    Los datos ya te llegan por parametro.
    // ---------------------------------------------------------------
    static void insertarSocio(Connection con, String nombre, String ciudad, String email) throws SQLException {
        String sql = "INSERT INTO socio (nombre, ciudad, email, fecha_alta) VALUES (?, ?, ?, CURDATE())";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, ciudad);
            ps.setString(3, email);
            System.out.println("INSERT: " + ps.executeUpdate() + " fila(s)");
        }
    }

    // ---------------------------------------------------------------
    // 3: cambia la ciudad del socio con ese id (UPDATE socio SET ciudad = ? WHERE id_socio = ?)
    //    con un PreparedStatement (2 "?") y muestra las filas afectadas
    //    (0 = ese socio no existe).
    //    Los datos ya te llegan por parametro.
    // ---------------------------------------------------------------
    static void cambiarCiudad(Connection con, int idSocio, String ciudad) throws SQLException {
        String sql = "UPDATE socio SET ciudad = ? WHERE id_socio = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ps.setInt(2, idSocio);
            System.out.println("UPDATE: " + ps.executeUpdate() + " fila(s)");
        }
    }

    // ---------------------------------------------------------------
    // 4: borra todas las inscripciones del socio con ese id
    //    (DELETE FROM inscripcion WHERE id_socio = ?)
    //    con un PreparedStatement (1 "?") y muestra las filas afectadas
    //    (pueden ser varias; 0 = ese socio no tiene inscripciones).
    // ---------------------------------------------------------------
    static void borrarInscripciones(Connection con, int idSocio) throws SQLException {
        String sql = "DELETE FROM inscripcion WHERE id_socio = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSocio);
            System.out.println("DELETE: " + ps.executeUpdate() + " fila(s)");
        }
    }
}
