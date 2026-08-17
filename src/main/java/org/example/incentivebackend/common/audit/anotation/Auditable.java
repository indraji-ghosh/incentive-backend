package org.example.incentivebackend.common.audit.anotation;

import org.example.incentivebackend.common.audit.enums.AuditModule;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    AuditModule module();

    String entity();

    String table();
}
