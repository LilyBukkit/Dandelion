package modloader;

import java.lang.annotation.Annotation;

public interface MLProp
    extends Annotation
{
    public abstract String name();

    public abstract String info();

    public abstract double min();

    public abstract double max();
}
