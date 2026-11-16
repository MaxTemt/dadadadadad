package com.airport.common.model;

import java.io.Serializable;

/**
 * Базовый класс всех сущностей предметной области.
 * Реализует сериализацию, сравнение и равенство по идентификатору.
 */
public abstract class AbstractEntity implements Serializable, Comparable<AbstractEntity> {

    private static final long serialVersionUID = 1L;

    private Long id;

    protected AbstractEntity() {
    }

    protected AbstractEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == null;
    }

    @Override
    public int compareTo(AbstractEntity other) {
        if (other == null) {
            return 1;
        }
        int byClass = getClass().getSimpleName().compareTo(other.getClass().getSimpleName());
        if (byClass != 0) {
            return byClass;
        }
        if (id == null && other.id == null) {
            return 0;
        }
        if (id == null) {
            return -1;
        }
        if (other.id == null) {
            return 1;
        }
        return Long.compare(id, other.id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AbstractEntity that = (AbstractEntity) object;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Long.hashCode(id);
    }
}
