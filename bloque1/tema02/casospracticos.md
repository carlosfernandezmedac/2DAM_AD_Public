# Casos Prácticos — 2. Flujos

---

### Ejemplo 1 — StreamTokenizer

Fichero de entrada `Ejemplo1.txt` (nótese que incluye líneas en blanco intermedias):
```
Hola Mundo soy 566 David



11234

Adios

    
     
      
      
```

```java
package TEMA02.Ejemplos;

import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

public class Ejemplo1 {
    public static void main(String[] args) throws Exception {

        StreamTokenizer streamTokenizer = new StreamTokenizer(new FileReader(".\\TEMA02\\Ejemplos\\Ejemplo1.txt"));

        // Configurar para que el carácter de nueva línea sea significativo
        streamTokenizer.eolIsSignificant(true);

        try {
            while(streamTokenizer.nextToken() != StreamTokenizer.TT_EOF){
                if(streamTokenizer.ttype == StreamTokenizer.TT_WORD) {
                    System.out.println("Word - "+streamTokenizer.sval);
                } else if(streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    System.out.println("number - "+streamTokenizer.nval);
                } else if(streamTokenizer.ttype == StreamTokenizer.TT_EOL) {
                    System.out.println("Fin");
                }
        }
        } catch (IOException e) {
        e.printStackTrace();
        }

    }
}
```

> 💡 Con `eolIsSignificant(true)` activado, cada línea en blanco genera igualmente un token `TT_EOL` ("Fin"). 

---

### Ejemplo 2 — LineNumberReader

Usa el mismo `Ejemplo2.txt` del ejemplo anterior.

```java
package TEMA02.Ejemplos;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Ejemplo2 {
    public static void main(String[] args) throws Exception {

        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(".\\TEMA02\\Ejemplos\\Ejemplo2.txt"));
            String line;
            while((line = lineNumberReader.readLine()) != null) {
                System.out.println("Contenido de la linea numero:"+ lineNumberReader.getLineNumber());
                System.out.println(line);
            }
                lineNumberReader.close();

            } catch (IOException e) {
            e.printStackTrace();
            }
    }
}
```

---

### Ejemplo 3 — DataInputStream / DataOutputStream

```java
package TEMA02.Ejemplos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Ejemplo3 {
    public static void main(String[] args) {
        try {

            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(".\\TEMA02\\Ejemplos\\data.txt"));
            dataOutputStream.writeInt(123);
            dataOutputStream.writeInt(987);
            dataOutputStream.writeFloat(123.45F);
            dataOutputStream.writeLong(789);
            dataOutputStream.writeDouble(9.3);
            dataOutputStream.close();

            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(".\\TEMA02\\Ejemplos\\data.txt"));
            int entero = dataInputStream.readInt();
            int entero1 = dataInputStream.readInt();
            float numeroFloat = dataInputStream.readFloat();
            double numerodouble = dataInputStream.readDouble();
            long numeroLong = dataInputStream.readLong();
            dataInputStream.close();
            System.out.println("El objeto de tipo float es: "+numeroFloat);
            System.out.println("El número entero es: "+entero);

            System.out.println("El double es :"+numerodouble);
            System.out.println("El objeto long es: "+numeroLong);


            } catch (FileNotFoundException e) {
            e.printStackTrace();
            } catch (IOException e) {
            e.printStackTrace();
            }
    }
}
```

> ⚠️ **Errores intencionados para detectar:** este ejemplo tiene dos fallos típicos de `DataInputStream`:
> 1. Se escriben 5 valores (`int`, `int`, `float`, `long`, `double`) pero se leen en **orden distinto** (`int`, `int`, `float`, `double`, `long`) — al leer el `double` en la posición donde en realidad hay un `long` escrito, el valor obtenido será basura sin sentido (no lanza excepción, simplemente da un resultado incorrecto).

---

