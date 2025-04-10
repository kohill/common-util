package com.companyname.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestInfo {

    String[] epicId() default {};

    String[] storyId() default {};

    String[] testId() default {};

}
