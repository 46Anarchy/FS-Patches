package dev.anarchy.fspatches.content.luckyblocks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventRegister {
    Class<? extends AbstractCustomLuckyBlock>[] value();
}
