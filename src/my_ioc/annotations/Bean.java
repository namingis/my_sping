package my_ioc.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {
	
}
