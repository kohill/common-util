package xray.model;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Xray {

    String requirement() default "";

    String id();

    String labels() default "";
}
