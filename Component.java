import java.lang.annotation.*;

/**
 * Anotación para marcar una clase como un componente Veld.
 * Los componentes son beans que pueden ser inyectados en otros componentes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    /**
     * Nombre del bean. Si no se especifica, se usará el nombre de clase en minúsculas.
     * 
     * @return nombre del bean
     */
    String value() default "";
}