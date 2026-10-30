import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class PracticaAula {

    private static final String URL      = "jdbc:mysql://localhost:3306/aula";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "CAMBIAR";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion = -1;

        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            Statement st = con.createStatement()) {
            System.out.println("Conexion OK");

            while (opcion != 0) {
                System.out.println("\n===== AULA =====");
                System.out.println("1. Listar portatiles");
                System.out.println("2. Insertar alumno");
                System.out.println("3. Ver portatil asignado");
                System.out.println("4. Cambiar portatil asignado");
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
                            listarPortatiles(st);
                            break;
                        case 2:
                            System.out.print("Nombre: ");
                            String nombre = sc.nextLine();
                            System.out.print("Apellidos: ");
                            String apellidos = sc.nextLine();
                            System.out.print("Email: ");
                            String email = sc.nextLine();
                            System.out.print("Codigo de tu portatil: ");
                            int codigo = Integer.parseInt(sc.nextLine().trim());
                            insertarAlumno(con, nombre, apellidos, email, codigo);
                            break;
                        case 3:
                            System.out.print("Codigo del portatil: ");
                            verPortatil(con, Integer.parseInt(sc.nextLine().trim()));
                            break;
                        case 4:
                            System.out.print("Tu email: ");
                            String emailU = sc.nextLine();
                            System.out.print("Codigo del nuevo portatil: ");
                            cambiarPortatil(con, emailU, Integer.parseInt(sc.nextLine().trim()));
                            break;
                        case 0:
                            System.out.println("Adios.");
                            break;
                        default:
                            System.out.println("Opcion no valida.");
                    }
                } catch (SQLException e) {
                    // si algo falla (portatil ocupado, no existe...) se avisa y se vuelve al menu
                    System.out.println("Error: " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("El codigo tiene que ser un numero.");
                }
            }

        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 1: muestra todos los portatiles con un Statement: codigo - ip - incidencias
    //         (SELECT codigo, ip, incidencias FROM portatil).
    //         Al final muestra cuantos portatiles ha encontrado la consulta.
    // ---------------------------------------------------------------
    static void listarPortatiles(Statement st) throws SQLException {
        
    }

    // ---------------------------------------------------------------
    // 2: inserta el alumno en la tabla alumno (nombre, apellidos, email, codigo_pc)
    //         con un PreparedStatement (4 "?") y muestra las filas afectadas.
    //         Los datos ya te llegan por parametro.
    // ---------------------------------------------------------------
    static void insertarAlumno(Connection con, String nombre, String apellidos, String email, int codigo) throws SQLException {
        
    }

    // ---------------------------------------------------------------
    // 3: busca el portatil por código y muestra su codigo, ip, incidencias y datos del alumno que lo tiene asignado
    //         Al final muestra cuantos portatiles ha encontrado la consulta.
    // ---------------------------------------------------------------
    static void verPortatil(Connection con, int codigo) throws SQLException {
    
    }

    // ---------------------------------------------------------------
    // 4: cambia el portatil del alumno con ese email:
    //         Muestra las filas afectadas (0 = ese email no existe).
    //         Prueba con un portatil ocupado y con uno que no existe:
    //         veras el error en pantalla y el programa vuelve al menu.
    // ---------------------------------------------------------------
    static void cambiarPortatil(Connection con, String email, int codigo) throws SQLException {
      
    }
}
