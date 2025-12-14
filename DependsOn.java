import java.lang.annotation.*;

/**
 * Anotación para especificar dependencias explícitas entre componentes.
 * Los componentes con esta anotación serán inicializados después de sus dependencias.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DependsOn {
    /**
     * Nombres de los componentes de los que depende este componente.
     * Los componentes listados serán inicializados antes que este componente.
     * 
     * @return array de nombres de beans dependientes
     */
    String[] value() default {};
}