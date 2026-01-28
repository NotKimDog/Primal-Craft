package net.kimdog_studios.primal_craft.client.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark config fields for automatic UI generation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigOption {
    /**
     * Display name for the option
     */
    String name();

    /**
     * Description/tooltip
     */
    String description() default "";

    /**
     * Tab category this option belongs to
     */
    ConfigTab tab();

    /**
     * Min value for numeric fields
     */
    double min() default 0.0;

    /**
     * Max value for numeric fields
     */
    double max() default 10.0;

    /**
     * Display order within tab (lower = higher)
     */
    int order() default 100;

    /**
     * Section within the tab
     */
    String section() default "";

    enum ConfigTab {
        GAMEPLAY,
        HUD,
        SYSTEMS,
        DIFFICULTY,
        ADVANCED
    }
}
