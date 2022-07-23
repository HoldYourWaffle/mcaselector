package net.querz.mcaselector;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be applied to a package, class or method to indicate that the class fields,
 * method return types and parameters in that element are not null by default unless:
 * <ul>
 *     <li>The method overrides a method in a superclass, in which case the annotation of the corresponding parameter in the superclass applies</li>
 *     <li>There is a default parameter annotation applied to a more tightly nested element.</li>
 * </ul>
 *
 * @see <a href="https://stackoverflow.com/q/16938241">StackOverflow</a>
 */
@Documented
@Nonnull
@TypeQualifierDefault({
    ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.LOCAL_VARIABLE,
    ElementType.METHOD,
    ElementType.PACKAGE,
    ElementType.PARAMETER,
    ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNullByDefault {}