package dev.anarchy.fspatches.registering.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterBlock {
    String value();

    Class<? extends net.minecraft.item.ItemBlock> itemBlock() default net.minecraft.item.ItemBlock.class;
}
